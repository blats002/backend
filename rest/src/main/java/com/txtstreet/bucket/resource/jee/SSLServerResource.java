package com..bucket.resource.jee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com..bucket.Configuration;
import com..bucket.resource.SSLResource;
import com..bucket.service.PuppetCallback;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import java.util.logging.Logger;

public class SSLServerResource extends BaseServerResource
    implements SSLResource {

    private static final Logger LOG
            = Logger.getLogger(SSLServerResource.class.getName());

    private static final String CERTIFICATE_HEADER = "-----BEGIN CERTIFICATE-----";
    private static final String CERTIFICATE_FOOTER = "-----END CERTIFICATE-----";
    private static final String PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";

    @Override
    public Representation post(Representation entity) {
        LOG.info("Domain: " + domain);
        LOG.info("Subdomain: " + subdomain);
        JSONObject responseObject = new JSONObject();
        if(!hasUserRole()) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }
        if(subdomain == null || subdomain.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }
        if(domain == null || domain.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }
        if(!checkDomainOwner(domain)) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            responseObject.put("code", Status.CLIENT_ERROR_UNAUTHORIZED.getCode());
            responseObject.put("reason", "Domain not owned");
            Representation representation = new StringRepresentation(responseObject.toJSONString());
            representation.setMediaType(MediaType.APPLICATION_JSON);
            return representation;
        }
        try {
            String body = entity.getText();
            JSONObject jsonObject = JSONObject.parseObject(body);
            String certificate = jsonObject.getString("certificate");
            String privateKey = jsonObject.getString("privateKey");
            if(certificate.startsWith(CERTIFICATE_HEADER) && certificate.endsWith(CERTIFICATE_FOOTER)
                    && privateKey.startsWith(PRIVATE_KEY_HEADER) && privateKey.endsWith(PRIVATE_KEY_FOOTER)) {
                jelasticService.writeCertificateAndPrivateKeyFile(domain, certificate, privateKey);
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
            //LOG.info(domain);
            //LOG.info(body);
            responseObject.put("code", Status.SUCCESS_OK.getCode());
            responseObject.put("reason", Status.SUCCESS_OK.getReasonPhrase());
            setStatus(Status.SUCCESS_OK);
        } catch (Exception e) {
            responseObject = new JSONObject();
            responseObject.put("code",Status.SERVER_ERROR_INTERNAL.getCode());
            responseObject.put("reason",Status.SERVER_ERROR_INTERNAL.getReasonPhrase());
            setStatus(Status.SERVER_ERROR_INTERNAL);
            e.printStackTrace();
        }
        Representation representation = new StringRepresentation(responseObject.toJSONString());
        representation.setMediaType(MediaType.APPLICATION_JSON);
        return representation;
    }

    private boolean checkDomainOwner(String domain) {
        boolean isOwner = false;
        try {
            JSONObject whereObject = new JSONObject();
            whereObject.put("name", domain);
            HttpResponse<String> quotaRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Domain")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = quotaRequest.getBody();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                for(int i=0;i<jsonArray.size();i++){
                    JSONObject domainObject = jsonArray.getJSONObject(i);
                    String name = domainObject.getString("name");
                    String objectId = domainObject.getString("objectId");
                    JSONObject appPointer = domainObject.getJSONObject("appId");
                    if(domain.equals(name)) {
                        // Found domain
                        String appId = appPointer.getString("objectId");
                        JSONObject application = getApplicationByUser(subdomain, userId);
                        if(application != null) {
                            LOG.info("Application: " + appId);
                            LOG.info("Application: " + application.getString("objectId"));
                            if(appId.equals(application.getString("objectId"))) {
                                isOwner = true;
                            }
                        } else {
                            LOG.info("No application found for " + domain);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOwner;
    }
}
