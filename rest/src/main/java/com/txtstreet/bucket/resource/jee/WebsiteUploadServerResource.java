package com..bucket.resource.jee;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.io.ByteStreams;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com..bucket.Configuration;
import com..bucket.resource.WebsiteUploadResource;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.tika.Tika;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WebsiteUploadServerResource extends BaseServerResource
        implements WebsiteUploadResource {

    private static final Logger LOG
            = Logger.getLogger(WebsiteUploadServerResource.class.getName());

    private String appObjectId = null;

    @Override
    public Representation post(Representation entity) {
        try {
            if(!hasUserRole()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }

            if(!isQuotaAndMeterExists()) {
                return createErrorResponse(Status.CLIENT_ERROR_NOT_FOUND);
            }

            if(isQuotaTraffic()) {
                return createErrorResponse(new Status(509, "Bandwidth Limit Exceeded"));
            }

            if(isQuotaStorage()) {
                return createErrorResponse(Status.SERVER_ERROR_INSUFFICIENT_STORAGE);
            }

            if(subdomain == null || subdomain.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }
            // Check if Subdomain exists
            // and owned by client
            JSONObject whereObject = new JSONObject();
            whereObject.put("appId", subdomain);

            HttpResponse<String> getRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Application")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = getRequest.getBody();
            //String appObjectId = null;
            if(getRequest.getStatus() == 200) {
                JSONObject results = JSON.parseObject(body);
                JSONArray resultsArray = results.getJSONArray("results");
                if(!resultsArray.isEmpty()){
                    for(int i=0;i<resultsArray.size();i++){
                        JSONObject jsonObject = resultsArray.getJSONObject(i);
                        LOG.info("jsonObject: " + jsonObject.toJSONString());
                        String appId = jsonObject.getString("objectId");
                        String appSubdomain = jsonObject.getString("appId");
                        JSONObject userPointer = jsonObject.getJSONObject("userId");
                        if(userPointer == null) {
                            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                            return null;
                        } else {
                            String id = userPointer.getString("objectId");
                            if(userId.equals(id) && subdomain.equals(appSubdomain)) {
                                appObjectId = appId;
                                break;
                            }
                        }
                    }
                }

                LOG.info("Subdomain: " + subdomain);
                LOG.info("Application ID: " + appObjectId);

                if(appObjectId == null || appObjectId.isEmpty()) {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return null;
                }

                if (entity != null) {
                    if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                        Request restletRequest = getRequest();
                        HttpServletRequest servletRequest = ServletUtils.getRequest(restletRequest);
                        ServletFileUpload upload = new ServletFileUpload();
                        FileItemIterator fileIterator = upload.getItemIterator(servletRequest);
                        getLogger().info("content type: " + servletRequest.getContentType());
                        getLogger().info("content: " + new ServletRequestContext(servletRequest).getContentLength());
                        getLogger().info("iterator: " + fileIterator.hasNext());

                        //////////////////
                        // Clean assets
                        /////////////////
                        clean(sessionToken, subdomain);

                        while (fileIterator.hasNext()) {
                            FileItemStream item = fileIterator.next();
                            String name = item.getName();
                            byte[] byteContent = ByteStreams.toByteArray(item.openStream());
                            // TODO byteContent is basically the file uploaded
                            LOG.info("contentName: " + name);
                            LOG.info("contentType: " + item.getContentType());
                            String result = processFile(sessionToken, byteContent, getQueryValue("upload_type"), appObjectId);
                            Representation response = new StringRepresentation(result);
                            response.setMediaType(MediaType.APPLICATION_JSON);
                            return response;
                        }
                    } else {
                        return badRequest();
                    }
                } else {
                    return badRequest();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalError();
        }
        return null;
    }
    private String processFile(final String sessionToken, final byte[] bytes, final String uploadType, final String appId) throws UnirestException {
        JSONObject result = new JSONObject();
        if("assets_zip".equals(uploadType)){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(bytes));
                        InputStreamReader isr = new InputStreamReader(zipStream);
                        ZipEntry ze;
                        Double unzippedSize = 0.00;

                        String user = getUser(sessionToken);
                        JSONObject userObject = JSONObject.parseObject(user);
                        String userId = userObject.getString("objectId");

                        while ((ze = zipStream.getNextEntry()) != null) {
                            String filePath = ze.getName();
                            long fileSize = ze.getCompressedSize();
                            getLogger().info("fileName: " + filePath);
                            getLogger().info("fileSize: " + fileSize);
                            ByteArrayOutputStream streamBuilder = new ByteArrayOutputStream();
                            int bytesRead;
                            byte[] tempBuffer = new byte[8192*2];
                            while ( (bytesRead = zipStream.read(tempBuffer)) != -1 ){
                                streamBuilder.write(tempBuffer, 0, bytesRead);
                            }
                            LOG.info("File path: " + filePath);
                            String filename = filePath.replaceFirst("(^.*[/\\\\])?([^/\\\\]*)$","$2");
                            LOG.info("File name: "+ filename);
                            byte[] byteArray = streamBuilder.toByteArray();
                            if(userId != null) {
                                writeFileToCloud(sessionToken, byteArray, filename, filePath, appId, userId);
                                unzippedSize = unzippedSize + byteArray.length;
                            }
                        }
                        if(userId != null) {
                            updateStorage(Double.valueOf(unzippedSize));
                            updateTraffic(Double.valueOf(bytes.length));

                        }
                        zipStream.close();
                    } catch (IOException e){
                        LOG.info("Error: " + e.getMessage());
                        e.printStackTrace();
                    } catch (UnirestException e) {
                        LOG.info("Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            setStatus(Status.SUCCESS_OK);
            result.put("success", "true");
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        return result.toJSONString();
    }

    private static void writeFileToCloud(String sessionToken, byte[] fileBytes, String fileName, String path, String appId, String userId)
            throws UnirestException, IOException {

        // Pre-process

        JSONObject appPointer = new JSONObject();
        appPointer.put("__type", "Pointer");
        appPointer.put("className", "Application");
        appPointer.put("objectId", appId);

        JSONObject file = new JSONObject();
        file.put("name", fileName);
        file.put("path", path);
        file.put("size", fileBytes.length);
        file.put("appId", appPointer);

        // Check if File exists
        JSONObject whereObject = new JSONObject();
        whereObject.put("name", fileName);
        whereObject.put("path", path);
        whereObject.put("appId", appPointer);

        HttpResponse<String> getRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                "/classes/File")
                .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                .header(X_PARSE_SESSION_TOKEN, sessionToken)
                .queryString("where", whereObject.toJSONString())
                .asString();

        boolean fileExist = false;

        if(getRequest.getStatus() == 200){
            JSONArray resultsArray = JSON.parseObject(getRequest.getBody())
                    .getJSONArray("results");
            if(!resultsArray.isEmpty()){
                fileExist = true;
                String existingObjectId = JSON.parseObject(getRequest.getBody())
                        .getJSONArray("results")
                        .getJSONObject(0)
                        .getString("objectId");
                LOG.info("File exists, updating: " + path);
                // Upload new file
                String mimeType = new Tika().detect(fileName);
                HttpResponse<String> response = Unirest.post(Configuration.TXTSTREET_PARSE_URL +
                        "/files/" + fileName)
                        .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                        .header(X_PARSE_SESSION_TOKEN, sessionToken)
                        .header("Content-Type", mimeType)
                        .body(fileBytes)
                        .asString();
                if(response.getStatus() == 201){
                    // Update existing File
                    String location = response.getHeaders().get("Location").get(0);
                    String filename = location.substring(location.lastIndexOf("/")+1, location.length());

                    JSONObject filePointer = new JSONObject();
                    filePointer.put("__type", "File");
                    filePointer.put("name", filename);
                    filePointer.put("url", location);

                    JSONObject updateObject = new JSONObject();
                    updateObject.put("size", fileBytes.length);
                    updateObject.put("filePointer", filePointer);
                    updateObject.put("createdAt", null);
                    updateObject.put("updatedAt", null);

                    JSONObject acl = new JSONObject();
                    JSONObject asterisk = new JSONObject();
                    asterisk.put("read", true);
                    asterisk.put("write", false);

                    JSONObject user = new JSONObject();
                    user.put("read", true);
                    user.put("write", true);

                    acl.put("*", asterisk);
                    acl.put(userId, user);
                    updateObject.put("ACL", acl);

                    HttpResponse<String> updateResponse = Unirest.put(Configuration.TXTSTREET_PARSE_URL +
                            "/classes/File/" + existingObjectId)
                            .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                            .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                            .header(X_PARSE_SESSION_TOKEN, sessionToken)
                            .header("Content-Type", "application/json")
                            .body(updateObject.toJSONString())
                            .asString();
                    LOG.info("Update Status:" + updateResponse.getStatusText());
                }
            }
        }

        if(!fileExist){
            // Upload file
            LOG.info("Uploading file: " + path);
            try {
                String mimeType = new Tika().detect(fileName);
                HttpResponse<String> response = Unirest.post(Configuration.TXTSTREET_PARSE_URL +
                        "/files/" + fileName)
                        .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                        .header(X_PARSE_SESSION_TOKEN, sessionToken)
                        .header("Content-Type", mimeType)
                        .body(fileBytes)
                        .asString();
                if(response.getStatus() == 201){
                    String location = response.getHeaders().get("Location").get(0);
                    String filename = location.substring(location.lastIndexOf("/")+1, location.length());
                    JSONObject filePointer = new JSONObject();
                    filePointer.put("__type", "File");
                    filePointer.put("name", filename);
                    filePointer.put("url", location);
                    file.put("filePointer", filePointer);

                    JSONObject acl = new JSONObject();
                    JSONObject asterisk = new JSONObject();
                    asterisk.put("read", true);
                    asterisk.put("write", false);

                    JSONObject user = new JSONObject();
                    user.put("read", true);
                    user.put("write", true);

                    acl.put("*", asterisk);
                    acl.put(userId, user);
                    file.put("ACL", acl);

                    file.put("createdAt", null);
                    file.put("updatedAt", null);

                    LOG.info(file.toJSONString());

                    // Associate to Parse Object
                    HttpResponse<String> res = Unirest.post(Configuration.TXTSTREET_PARSE_URL +
                            "/classes/File")
                            .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                            .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                            .header(X_PARSE_SESSION_TOKEN, sessionToken)
                            .header("Content-Type", "application/json")
                            .body(file.toJSONString())
                            .asString();
                    LOG.info("Status:" + res.getBody());

                }
            } catch (UnirestException e){
                LOG.info("Failed to upload: " + path);
            } catch (Exception e) {
                LOG.info("Failed to upload: " + e.getLocalizedMessage());
            }
        }

    }

    /*
    if(clean != null && clean.equalsIgnoreCase("true")) {
                        JSONObject param = new JSONObject();
                        param.put("appId", appId);
                        HttpResponse<String> res = Unirest.post(Configuration.PARSE_URL +
                                "/functions/clean")
                                .header("X-Parse-Application-Id", Configuration.PARSE_APP_ID)
                                .header("X-Parse-REST-API-Key", Configuration.PARSE_REST_API_KEY)
                                .header("X-Parse-Session-Token", sessionToken)
                                .header("X-Parse-Revocable-Session", "1")
                                .header("Content-Type", "application/json")
                                .body(param.toString())
                                .asString();
                        if(res.getBody().contains("success")) {
                            System.out.println("Success Clean files");
                        } else {
                            System.out.println("Failed Clean files");
                            return;
                        }
                    }
     */
    protected void clean(String sessionToken, String appId) {
        try {
            Double appByteSize = calculateAppUsedStorage();
            JSONObject param = new JSONObject();
            param.put("appId", appId);
            HttpResponse<String> res = Unirest.post(Configuration.TXTSTREET_PARSE_URL +
                    "/functions/clean")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .header("X-Parse-Revocable-Session", "1")
                    .header("Content-Type", "application/json")
                    .body(param.toString())
                    .asString();
            if(res.getBody().contains("success")) {
                LOG.info("Success clean files: " + appId);
                LOG.info("App size: " + appByteSize);
                updateStorage(-appByteSize);
            } else {
                LOG.info("Failed clean files: " + appId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateStorage(Double value) {
        try {
            Double byteSize = Double.valueOf(value);
            JSONObject whereObject = new JSONObject();
            whereObject.put("user", createPointer("_User", userId));
            HttpResponse<String> getRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Meter")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = getRequest.getBody();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String objectId = jsonObject.getString("objectId");
                Double storage = jsonObject.getDouble("storage");
                LOG.info("Old storage value=" + storage);
                storage = storage + byteSize;
                LOG.info("New storage value=" + storage);
                jsonObject.put("storage", storage);
                jsonObject.put("createdAt", null);
                jsonObject.put("updatedAt", null);
                HttpResponse<String> putRequest = Unirest.put(Configuration.TXTSTREET_PARSE_URL +
                        "/classes/Meter/" + objectId)
                        .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                        .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                        .body(jsonObject.toJSONString())
                        .asString();
                String responseBody = putRequest.getBody();
                LOG.info("Update response: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTraffic(Double b) {
        try {
            Double byteSize = Double.valueOf(b);
            JSONObject whereObject = new JSONObject();
            whereObject.put("user", createPointer("_User", userId));
            HttpResponse<String> getRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/Meter")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_PARSE_SESSION_TOKEN, sessionToken)
                    .queryString("where", whereObject.toJSONString())
                    .asString();
            String body = getRequest.getBody();
            JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
            if(!jsonArray.isEmpty()) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String objectId = jsonObject.getString("objectId");
                Double traffic = jsonObject.getDouble("traffic");
                traffic = traffic + byteSize;
                jsonObject.put("traffic", traffic);
                jsonObject.put("createdAt", null);
                jsonObject.put("updatedAt", null);
                HttpResponse<String> putRequest = Unirest.put(Configuration.TXTSTREET_PARSE_URL +
                        "/classes/Meter/" + objectId)
                        .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                        .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                        .body(jsonObject.toJSONString())
                        .asString();
                String responseBody = putRequest.getBody();
                LOG.info("Update response: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*
Parse.Cloud.useMasterKey();
    var appId = req.params.appId;
    var appQuery = new Parse.Query("Application");
    appQuery.equalTo("objectId", appId);
    appQuery.find({
        success: function (result) {
            //res.success(result);
            console.log(result);
            //res.success(result[0]);
            //var _objectId = result[0].id;
            var pointer = { __type:"Pointer", className:"Application", objectId:appId };

            var query = new Parse.Query("File");
            query.equalTo("appId", pointer);

            query.find().then(function (files) {
                Parse.Object.destroyAll(files).then(function() {
                    res.success("success");
                });
            }, function (error) {
                response.error(error);
            });
        },
        error: function () {
            res.error("unable to get Application object");
        }
    });
 */
    protected Double calculateAppUsedStorage() throws Exception {
        // Check if File exists
        Double totalSize = 0.00;
        JSONObject whereObject = new JSONObject();
        whereObject.put("appId", createPointer("Application", appObjectId));
        HttpResponse<String> getRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                "/classes/File")
                .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                .header(X_PARSE_SESSION_TOKEN, sessionToken)
                .queryString("where", whereObject.toJSONString())
                .asString();
        String body = getRequest.getBody();
        JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
        if(!jsonArray.isEmpty()) {
            for(int i=0;i<jsonArray.size();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Double size = jsonObject.getDouble("size");
                if(size != null)
                    totalSize = totalSize + size;
            }
        }
        return totalSize;
    }

    public byte[] getFile(String fileName){
        // TODO: Get file and put bytes here
        return null;
    }

    private Representation createErrorResponse(Status status) {
        JSONObject result = new JSONObject();
        result.put("error", status.getCode());
        result.put("message", status.getReasonPhrase());
        StringRepresentation representation = new StringRepresentation(result.toJSONString());
        representation.setMediaType(MediaType.APPLICATION_JSON);
        setStatus(status);
        return representation;
    }

}
