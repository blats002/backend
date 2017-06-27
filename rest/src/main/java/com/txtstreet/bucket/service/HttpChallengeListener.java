package com..bucket.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com..bucket.Configuration;
import com..bucket.resource.jee.BaseServerResource;
import it.zero11.acme.AcmeChallengeListener;
import org.apache.tika.Tika;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class HttpChallengeListener implements AcmeChallengeListener, BaseService{

    private static final Logger LOG
            = Logger.getLogger(HttpChallengeListener.class.getName());

    private final String host;
    private final String appId;
    private final String userId;
    private final String webroot;
    private final String session;

    public HttpChallengeListener(String session, String appId, String userId, String host, String webroot) {
        this.host = host;
        this.webroot = webroot;
        this.session = session;
        this.appId = appId;
        this.userId = userId;
    }

    @Override
    public boolean challengeHTTP01(String domain, String token, String challengeURI, String challengeBody) {
        return createChallengeFiles(token, challengeBody);
    }

    private boolean createChallengeFiles(String token, String challengeBody) {
        boolean success = false;
        try {
            System.out.println("Token: " + token);
            System.out.println("Challenge Body: " + challengeBody);
            String fileName = token;
            String filePath = ".well-known/acme-challenge/" + token;
            return writeFileToCloud(session, challengeBody.getBytes(), fileName, filePath, appId, userId);
        } catch (Exception e){
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public void challengeCompleted(String domain) {
        deleteChallengeFiles();
    }

    private void deleteChallengeFiles() {
        /*
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(host);
            if(!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                return;
            }

            ftp.login(username, password);
            ftp.changeWorkingDirectory(webroot);
            ftp.changeWorkingDirectory(".well-known");
            ftp.changeWorkingDirectory("acme-challenge");

            FTPFile[] subFiles = ftp.listFiles();

            if (subFiles != null && subFiles.length > 0) {
                for (FTPFile aFile : subFiles) {
                    String currentFileName = aFile.getName();
                    if (currentFileName.equals(".") || currentFileName.equals("..")) {
                        continue;
                    }else{
                        ftp.deleteFile(currentFileName);
                    }
                }
            }
            ftp.changeToParentDirectory();
            ftp.removeDirectory("acme-challenge");
            ftp.changeToParentDirectory();
            ftp.removeDirectory(".well-known");
            ftp.logout();
        } catch(IOException e) {
            throw new AcmeException(e);
        } finally {
            if(ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch(IOException ioe) {
                }
            }
        }
        */
    }

    @Override
    public void challengeFailed(String domain) {
        deleteChallengeFiles();
    }

    protected static boolean writeFileToCloud(String sessionToken, byte[] fileBytes, String fileName, String path, String appId, String userId)
            throws UnirestException, IOException {

        LOG.info("Session Token: " + sessionToken);
        LOG.info("Use ID: " + userId);
        LOG.info("App ID: " + appId);
        LOG.info("File Name: " + fileName);
        LOG.info("File Path: " + path);

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

        LOG.info("========================================================");
        LOG.info(whereObject.toJSONString());
        LOG.info("========================================================");

        HttpResponse<String> getRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                "/classes/File")
                .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                .header(X_PARSE_SESSION_TOKEN, sessionToken)
                .queryString("where", whereObject.toJSONString())
                .asString();

        boolean fileExist = false;

        if(getRequest.getStatus() == 200){
            LOG.info("Status 200");
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
//                        .header("Content-Type", mimeType)
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

                    LOG.info("========================================================");
                    LOG.info("UPDATE OBJECT");
                    LOG.info("========================================================");
                    LOG.info(updateObject.toJSONString());
                    LOG.info("========================================================");

                    HttpResponse<String> updateResponse = Unirest.put(Configuration.TXTSTREET_PARSE_URL +
                            "/classes/File/" + existingObjectId)
                            .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                            .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                            .header(X_PARSE_SESSION_TOKEN, sessionToken)
                            .header("Content-Type", "application/json")
                            .body(updateObject.toJSONString())
                            .asString();
                    LOG.info("Update Status:" + updateResponse.getStatusText());
                    LOG.info("Update Body:" + updateResponse.getBody());
                    return true;
                }
            } else {
                LOG.info("Empty response");
                // Upload new file

                String mimeType = new Tika().detect(fileName);
                HttpResponse<String> response = Unirest.post(Configuration.TXTSTREET_PARSE_URL +
                        "/files/" + fileName)
                        .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                        .header(X_PARSE_SESSION_TOKEN, sessionToken)
//                        .header("Content-Type", mimeType)
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

                    JSONObject newObject = new JSONObject();
                    newObject.put("size", fileBytes.length);
                    newObject.put("filePointer", filePointer);
                    newObject.put("createdAt", null);
                    newObject.put("updatedAt", null);

                    JSONObject acl = new JSONObject();
                    JSONObject asterisk = new JSONObject();
                    asterisk.put("read", true);
                    asterisk.put("write", false);

                    JSONObject user = new JSONObject();
                    user.put("read", true);
                    user.put("write", true);

                    acl.put("*", asterisk);
                    acl.put(userId, user);
                    newObject.put("ACL", acl);

                    newObject.put("name", fileName);
                    newObject.put("path", path);
                    newObject.put("size", fileBytes.length);
                    newObject.put("appId", appPointer);

                    LOG.info("========================================================");
                    LOG.info("NEW OBJECT");
                    LOG.info("========================================================");
                    LOG.info(newObject.toJSONString());
                    LOG.info("========================================================");

                    HttpResponse<String> updateResponse = Unirest.post(Configuration.TXTSTREET_PARSE_URL +
                            "/classes/File")
                            .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                            .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                            .header(X_PARSE_SESSION_TOKEN, sessionToken)
                            .header("Content-Type", "application/json")
                            .body(newObject.toJSONString())
                            .asString();
                    LOG.info("Create Status:" + updateResponse.getStatusText());
                    LOG.info("Create Body:" + updateResponse.getBody());
                    return true;
                }
            }
        }
        return false;
    }
}
