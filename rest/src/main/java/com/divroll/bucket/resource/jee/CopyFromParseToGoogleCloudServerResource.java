package com.divroll.bucket.resource.jee;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.mashape.unirest.http.Unirest;
import com.divroll.bucket.Configuration;
import com.divroll.bucket.GoogleJsonKey;
import com.divroll.bucket.resource.CopyFromParseToGoogleCloud;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CopyFromParseToGoogleCloudServerResource extends BaseServerResource
    implements CopyFromParseToGoogleCloud {
    private static final Logger LOG
            = Logger.getLogger(CopyFromParseToGoogleCloudServerResource.class.getName());
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    @Override
    public Representation get(Representation entity) {
        if(!hasMasterRole()) {
            return unauthorized();
        }
        JSONObject result = new JSONObject();
        final HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        try {
            long count = countFiles();
            long chops = 1;
            if(count > 100) {
                chops = count / 100;
                if ((chops % 100) != 0) {
                    chops++;
                }
            }
            for(long i=0;i<chops;i++){
                LOG.info("progress " + i + "/" + (chops - 1));
                JSONObject where = new JSONObject();
                JSONObject exist = new JSONObject();
                exist.put("$exists", false);
                where.put("isMigratedToGoogleStorage", exist);
                com.mashape.unirest.http.HttpResponse<String> getRequest = Unirest.get(Configuration.DIVROLL_PARSE_URL +
                        "/classes/File")
                        .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
                        .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                        .queryString("limit", 100)
                        .queryString("skip", i * 100)
                        .queryString("where", where.toJSONString())
                        .asString();
                String body = getRequest.getBody();
                final JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
                if(!jsonArray.isEmpty()) {
                    for(int j=0;j<jsonArray.size();j++) {
                        final int idx = j;
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("Run index " + idx);
                                String objectId = null;
                                try {
                                    JSONObject jsonObject = jsonArray.getJSONObject(idx);
                                    JSONObject filePonter = jsonObject.getJSONObject("filePointer");
                                    JSONObject appPointer = jsonObject.getJSONObject("appId");
                                    String filePath = jsonObject.getString("path");
                                    String fileUrl = filePonter.getString("url");
                                    if(fileUrl.startsWith("http://localhost:8080/parse")){
                                        fileUrl = fileUrl.replace("http://localhost:8080/parse", Configuration.DIVROLL_PARSE_URL);
                                    } else if(fileUrl.startsWith("https://parse.divroll.com/parse")) {
                                        fileUrl = fileUrl.replace(Configuration.DIVROLL_PARSE_URL, Configuration.DIVROLL_PARSE_URL);
                                    }
                                    System.out.println("File URL: " + fileUrl);
                                    HttpRequest fileRequest = requestFactory.buildGetRequest(new GenericUrl(fileUrl));
                                    HttpResponse fileRequestResponse = fileRequest.execute();
                                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    fileRequestResponse.download(out);
                                    long fileSize = out.size();
                                    objectId = jsonObject.getString("objectId");

                                    InputStream stream = new ByteArrayInputStream(GoogleJsonKey.JSON_KEY.getBytes(StandardCharsets.UTF_8));
                                    StorageOptions options = StorageOptions.newBuilder()
                                            .setProjectId(PROJECT_ID)
                                            .setCredentials(GoogleCredentials.fromStream(stream)).build();
                                    Storage storage = options.getService();

                                    com.mashape.unirest.http.HttpResponse<String> quotaRequest = Unirest.get(Configuration.DIVROLL_PARSE_URL +
                                            "/classes/Application/" + appPointer.getString("objectId"))
                                            .header("X-Parse-Application-Id", Configuration.DIVROLL_PARSE_APP_ID)
                                            .header("X-Parse-REST-API-Key", Configuration.DIVROLL_PARSE_REST_API_KEY)
                                            .header("X-Parse-Revocable-Session", "1")
                                            .asString();
                                    String appPointerGetResponse = quotaRequest.getBody();
                                    System.out.println("Body: " + appPointerGetResponse);
                                    if(appPointerGetResponse != null) {
                                        JSONObject appPointerGetResponseObj = JSONObject.parseObject(appPointerGetResponse);
                                        LOG.info("Body: " + appPointerGetResponseObj.toJSONString());
                                        String subdomain = appPointerGetResponseObj.getString("appId");
                                        String bucketName = "appsbucket";

                                        List<Acl> acls = new ArrayList<>();
                                        acls.add(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

                                        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, subdomain + "/" + filePath)
                                                .setAcl(acls)
                                                .build();
                                        Blob blob = storage.create(blobInfo, out.toByteArray());

                                        String md5 = blob.getMd5();
                                        String mediLink = blob.getMediaLink();

                                        BlobId id = blob.getBlobId();
                                        String blobBucket = id.getBucket();
                                        String blobName = id.getName();
                                        Long blobGeneration = id.getGeneration();

                                        LOG.info("MD5: " + md5);
                                        LOG.info("Link: " + mediLink);
                                        LOG.info("Bucket: " + blobBucket);
                                        LOG.info("Name: " + blobName);
                                        LOG.info("Generation: " + blobGeneration);
                                        updateMigrateStatus(objectId, true);
                                    } else {
                                        //updateMigrateStatus(objectId, false);
                                    }
                                    //updateFileSize(objectId, fileSize);
                                    //totalFileSize = totalFileSize + fileSize;
                                    LOG.info("File objectId: " + objectId);
                                    LOG.info("File url: " + fileUrl);
                                    LOG.info("File size: " + fileSize);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //updateMigrateStatus(objectId, false);
                                }
                            }
                        };
                        LOG.info("Progress " + i + "/" + (chops - 1));
                        LOG.info("Index " + j + "/" + jsonArray.size());
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }
                }
            }

            setStatus(Status.SUCCESS_OK);
            result.put("success", true);
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            result.put("error", Status.SERVER_ERROR_INTERNAL.getCode());
            result.put("message", Status.SERVER_ERROR_INTERNAL.getReasonPhrase());
            e.printStackTrace();
        }
        Representation representation = new StringRepresentation(result.toJSONString());
        representation.setMediaType(MediaType.APPLICATION_JSON);
        return representation;
    }

    private long countFiles() throws IOException {
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        GenericUrl url = new GenericUrl(Configuration.DIVROLL_PARSE_URL + "/classes/File");
        url.put("limit", 0);
        url.put("count", 1);
        HttpRequest httpRequest = requestFactory.buildGetRequest(url);
        httpRequest.getHeaders().set("X-Parse-Application-Id", Configuration.DIVROLL_PARSE_APP_ID);
        httpRequest.getHeaders().set("X-Parse-REST-API-Key", Configuration.DIVROLL_PARSE_REST_API_KEY);
        httpRequest.getHeaders().set("X-Parse-Master-Key", Configuration.DIVROLL_MASTER_KEY);
        HttpResponse response = httpRequest.execute();
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
            JSONObject fileResult = JSON.parseObject(line);
            return fileResult.getLongValue("count");
        }
        return -1; // means, error
    }
    private void saveCalculatedSize(Long count, Long totalFileSize) {
        try {
            JSONObject saveObject = new JSONObject();
            saveObject.put("count", count);
            saveObject.put("totalFileSize", totalFileSize);

            JSONObject acl = new JSONObject();
            JSONObject asterisk = new JSONObject();
            asterisk.put("read", false);
            asterisk.put("write", false);
            acl.put("*", asterisk);
            saveObject.put("ACL", acl);

            com.mashape.unirest.http.HttpResponse<String> saveResponse = Unirest.post(Configuration.DIVROLL_PARSE_URL +
                    "/classes/TotalFile")
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    .header("Content-Type", "application/json")
                    .body(saveObject.toJSONString())
                    .asString();
            LOG.info("Save Status " + saveResponse.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateFileSize(String fileId, long size) {
        try {
            JSONObject updateObject = new JSONObject();
            updateObject.put("size", size);
            updateObject.put("createdAt", null);
            updateObject.put("updatedAt", null);

            JSONObject acl = new JSONObject();
            JSONObject asterisk = new JSONObject();
            asterisk.put("read", true);
            asterisk.put("write", false);
            acl.put("*", asterisk);
            updateObject.put("ACL", acl);

            com.mashape.unirest.http.HttpResponse<String> updateResponse = Unirest.put(Configuration.DIVROLL_PARSE_URL +
                    "/classes/File/" + fileId)
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    .header("Content-Type", "application/json")
                    .body(updateObject.toJSONString())
                    .asString();
            LOG.info("Update Status " + fileId + ": " + updateResponse.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateMigrateStatus(String fileId, boolean isMigrated) {
        try {
            JSONObject updateObject = new JSONObject();
            updateObject.put("isMigratedToGoogleStorage", isMigrated);
            updateObject.put("createdAt", null);
            updateObject.put("updatedAt", null);

            JSONObject acl = new JSONObject();
            JSONObject asterisk = new JSONObject();
            asterisk.put("read", true);
            asterisk.put("write", false);
            acl.put("*", asterisk);
            updateObject.put("ACL", acl);

            com.mashape.unirest.http.HttpResponse<String> updateResponse = Unirest.put(Configuration.DIVROLL_PARSE_URL +
                    "/classes/File/" + fileId)
                    .header(X_PARSE_APPLICATION_ID, Configuration.DIVROLL_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.DIVROLL_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.DIVROLL_MASTER_KEY)
                    .header("Content-Type", "application/json")
                    .body(updateObject.toJSONString())
                    .asString();
            LOG.info("Update Status " + fileId + ": " + updateResponse.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
