package com..bucket.resource.jee;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com..bucket.Configuration;
import com..bucket.service.ACMEService;
import com..bucket.service.BaseService;
import com..bucket.service.JelasticService;
import org.apache.tika.Tika;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BaseServerResource extends ServerResource
    implements BaseService {

    private static final Logger LOG
            = Logger.getLogger(BaseServerResource.class.getName());

    protected static final long MEGABYTE_BYTE_MULTIPLIER = 1000000;
    protected static final String FREE_STORAGE_QUOTA_CONFIG  = "FREE_STORAGE_QUOTA";
    protected static final String FREE_TRAFFIC_QUOTA_CONFIG  = "FREE_TRAFFIC_QUOTA";
    protected static final String FREE_COMPUTE_QUOTA_CONFIG  = "FREE_COMPUTE_QUOTA";

    protected Map<String,String> headers = new LinkedHashMap<String,String>();

    protected String user = null;
    protected String userId = null;
    protected String sessionToken = null;
    protected String subdomain;
    protected String domain;

    protected JelasticService jelasticService;
    protected ACMEService acmeService;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        subdomain = getAttribute("subdomain");
        domain = getAttribute("domain");
        getHeaders();
        jelasticService = new JelasticService();
        acmeService = new ACMEService();
    }

    protected String getUser(String sessionToken) {
        try {
            HttpResponse<String> appGet = Unirest.get(Configuration.TXTSTREET_PARSE_URL + Configuration.ME_URI)
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .asString();
            LOG.info("Get User response: " + appGet.getBody());
            JSONObject appResult = JSON.parseObject(appGet.getBody());
            LOG.info("Retrieved User from token: " + appResult.getString("objectId"));
            return appResult.toJSONString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    protected void getHeaders() {
        LOG.info("Get headers");
        HttpServletRequest httpRequest = ServletUtils.getRequest(getRequest());
        for (Enumeration<String> e = httpRequest.getHeaderNames(); e.hasMoreElements();) {
            String headerName = e.nextElement();
            String header = httpRequest.getHeader(headerName);
            headers.put(headerName, header);
            LOG.info("===================================================================");
            LOG.info("Header: " + headerName);
            LOG.info("Header Value: " + header);
            LOG.info("===================================================================");

        }
        //return headers;
    }
    protected JSONObject getApplicationByUser(String subdomain, String userId) {
        try {
            JSONObject whereObject = new JSONObject();
            whereObject.put("appId", subdomain);
            HttpResponse<String> quotaRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Application")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = quotaRequest.getBody();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                for(int i=0;i<jsonArray.size();i++){
                    JSONObject item = jsonArray.getJSONObject(i);
                    if(item.getString("appId") != null && item.getString("appId").equals(subdomain)) {
                        JSONObject userPointer = item.getJSONObject("userId");
                        if(userPointer != null && userPointer.getString("objectId") != null) {
                            if(userPointer.getString("objectId").equals(userId)) {
                                LOG.info("Subdomain: " + subdomain);
                                LOG.info("User ID: " + userPointer.getString("objectId"));
                                LOG.info("User ID: " + userId);
                                return item;
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    protected boolean hasUserRole() {
        String headerField = X_PARSE_SESSION_TOKEN;
        String sessionToken = headers.get(headerField.toLowerCase());
        if(sessionToken == null || sessionToken.isEmpty()) {
            sessionToken = headers.get(X_PARSE_SESSION_TOKEN);
        }
        LOG.info(headerField);
        this.sessionToken = sessionToken;
        if(sessionToken != null) {
            user = getUser(sessionToken);
            if(user != null) {
                userId = JSONObject.parseObject(user).getString("objectId");
            }
        }
        LOG.info("Session token: " + sessionToken);
        LOG.info("User: " + user);
        LOG.info("User ID: " + userId);
        return user != null;
    }

    // TODO: Implement a better algo for this
    protected boolean isQuotaAndMeterExists() {
        Boolean isExist = false;
        try {
            JSONObject whereObject = new JSONObject();
            whereObject.put("user", createPointer("_User", userId));
            HttpResponse<String> quotaRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Quota")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = quotaRequest.getBody();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                HttpResponse<String> meterRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                        "/classes/Meter")
                        .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                        .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                        .queryString("where", whereObject.toJSONString())
                        .asString();
                String meterResponseBody = meterRequest.getBody();
                JSONArray jsonMeterArray = JSONObject.parseObject(meterResponseBody).getJSONArray("results");
                if(!jsonMeterArray.isEmpty()) {
                    isExist = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExist;
    }

    protected boolean isQuotaExists() {
        Boolean isExist = false;
        try {
            JSONObject whereObject = new JSONObject();
            whereObject.put("user", createPointer("_User", userId));
            HttpResponse<String> quotaRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Quota")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = quotaRequest.getBody();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                isExist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("Quota: " + isExist);
        return isExist;
    }

    protected boolean isMeterExists() {
        Boolean isExist = false;
        try {
            JSONObject whereObject = new JSONObject();
            whereObject.put("user", createPointer("_User", userId));
            HttpResponse<String> quotaRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Meter")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = quotaRequest.getBody();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                isExist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.info("Meter: " + isExist);
        return isExist;
    }

    protected void createQuota() {
        LOG.info("Creating Quota object");
        if(getParseConfig().isEmpty()) {
            return;
        }
        if(getParseConfig().getDouble(FREE_STORAGE_QUOTA_CONFIG) == null
                || getParseConfig().getDouble(FREE_TRAFFIC_QUOTA_CONFIG) == null
                || getParseConfig().getDouble(FREE_COMPUTE_QUOTA_CONFIG) == null
                ) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("storage", getParseConfig().getDoubleValue(FREE_STORAGE_QUOTA_CONFIG) * MEGABYTE_BYTE_MULTIPLIER);
            jsonObject.put("traffic", getParseConfig().getDoubleValue(FREE_TRAFFIC_QUOTA_CONFIG) * MEGABYTE_BYTE_MULTIPLIER);
            jsonObject.put("compute", getParseConfig().getDoubleValue(FREE_COMPUTE_QUOTA_CONFIG) * MEGABYTE_BYTE_MULTIPLIER);

            JSONObject acl = new JSONObject();
            JSONObject asterisk = new JSONObject();
            asterisk.put("read", true);
            asterisk.put("write", false);
            acl.put("*", asterisk);

            jsonObject.put("ACL", acl);
            jsonObject.put("user", createPointer("_User", userId));

            HttpResponse<String> response = Unirest.post(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Quota")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .header("Content-Type", "application/json")
                    .body(jsonObject.toJSONString())
                    .asString();
            String body = response.getBody();
            LOG.info(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createMeter() {
        LOG.info("Creating Meter object");
        if(getParseConfig().isEmpty()) {
            return;
        }
        if(getParseConfig().getDouble(FREE_STORAGE_QUOTA_CONFIG) == null
                || getParseConfig().getDouble(FREE_TRAFFIC_QUOTA_CONFIG) == null
                || getParseConfig().getDouble(FREE_COMPUTE_QUOTA_CONFIG) == null
                ) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("storage", 0);
            jsonObject.put("traffic", 0);
            jsonObject.put("compute", 0);

            JSONObject acl = new JSONObject();
            JSONObject asterisk = new JSONObject();
            asterisk.put("read", true);
            asterisk.put("write", false);
            acl.put("*", asterisk);

            jsonObject.put("ACL", acl);
            jsonObject.put("user", createPointer("_User", userId));

            HttpResponse<String> response = Unirest.post(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Meter")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .header("Content-Type", "application/json")
                    .body(jsonObject.toJSONString())
                    .asString();
            String body = response.getBody();
            LOG.info(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean isQuotaStorage() {
        Boolean isQuota = true;
        String key = "storage";
        try {
            JSONObject whereObject = new JSONObject();
            whereObject.put("user", createPointer("_User", userId));
            HttpResponse<String> quotaRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Quota")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = quotaRequest.getBody();
            LOG.info(body);
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                Double storageQuota = jsonObject.getDouble(key);
                HttpResponse<String> getRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                        "/classes/Meter")
                        .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                        .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                        .queryString("where", whereObject.toJSONString())
                        .asString();
                String usageBody = getRequest.getBody();
                JSONArray usageJsonArray = JSONObject.parseObject(usageBody).getJSONArray("results");
                if(!usageJsonArray.isEmpty()) {
                    JSONObject usageJsonObject = usageJsonArray.getJSONObject(0);
                    Double storageUsage = usageJsonObject.getDouble(key);
                    LOG.info("=================================");
                    LOG.info("Storage Quota: " + storageQuota);
                    LOG.info("Storage Usage: " + storageUsage);
                    LOG.info("=================================");
                    if(storageUsage < storageQuota) {
                        isQuota = false;
                    } else {
                        isQuota = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isQuota;
    }

    protected boolean isQuotaTraffic() {
        Boolean isQuota = true;
        String key = "traffic";
        try {
            JSONObject whereObject = new JSONObject();
            whereObject.put("user", createPointer("_User", userId));
            HttpResponse<String> quotaRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Quota")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = quotaRequest.getBody();
            LOG.info(body);
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                Double trafficQuota = jsonObject.getDouble(key);
                HttpResponse<String> usageRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                        "/classes/Meter")
                        .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                        .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                        .queryString("where", whereObject.toJSONString())
                        .asString();
                String usageBody = usageRequest.getBody();
                JSONArray usageJsonArray = JSONObject.parseObject(usageBody).getJSONArray("results");
                if(!usageJsonArray.isEmpty()) {
                    JSONObject usageJsonObject = usageJsonArray.getJSONObject(0);
                    Double trafficUsage = usageJsonObject.getDouble(key);
                    LOG.info("=================================");
                    LOG.info("Traffic Quota: " + trafficQuota);
                    LOG.info("Traffic Usage: " + trafficUsage);
                    LOG.info("=================================");
                    if(trafficUsage < trafficQuota) {
                        isQuota = false;
                    } else {
                        isQuota = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isQuota;
    }

    protected JSONObject createPointer(String className, String objectId) {
        JSONObject pointer = new JSONObject();
        pointer.put("__type", "Pointer");
        pointer.put("className", className);
        pointer.put("objectId", objectId);
        return pointer;
    }

    protected JSONObject getParseConfig() {
        JSONObject jsonObject = new JSONObject();
        try {
            HttpResponse<String> configRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL + Configuration.CONFIG_URI)
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                    .asString();
            String body = configRequest.getBody();
            JSONObject jso = JSONObject.parseObject(body);
            jsonObject = jso;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    protected Representation badRequest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", Status.CLIENT_ERROR_BAD_REQUEST.getCode());
        jsonObject.put("error", Status.CLIENT_ERROR_BAD_REQUEST.getReasonPhrase());
        Representation response = new StringRepresentation(jsonObject.toJSONString());
        response.setMediaType(MediaType.APPLICATION_JSON);
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return response;
    }

    protected Representation internalError() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", Status.SERVER_ERROR_INTERNAL.getCode());
        jsonObject.put("error", Status.SERVER_ERROR_INTERNAL.getReasonPhrase());
        Representation response = new StringRepresentation(jsonObject.toJSONString());
        response.setMediaType(MediaType.APPLICATION_JSON);
        setStatus(Status.SERVER_ERROR_INTERNAL);
        return response;
    }


}
