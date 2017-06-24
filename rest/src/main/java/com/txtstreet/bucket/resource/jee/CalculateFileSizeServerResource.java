package com..bucket.resource.jee;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.api.client.http.*;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.io.CountingOutputStream;
import com.mashape.unirest.http.*;
import com..bucket.Configuration;
import com..bucket.resource.CalculateFileSizeResource;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class CalculateFileSizeServerResource extends BaseServerResource
    implements CalculateFileSizeResource {
    private static final Logger LOG
            = Logger.getLogger(CalculateFileSizeServerResource.class.getName());
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    @Override
    public Representation get(Representation entity) {
        JSONObject result = new JSONObject();
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        try {
            long totalFileSize = 0;
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
                com.mashape.unirest.http.HttpResponse<String> getRequest = Unirest.get(Configuration.TXTSTREET_PARSE_URL +
                        "/classes/File")
                        .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                        .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                        .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                        .queryString("limit", 100)
                        .queryString("skip", i * 100)
                        .asString();
                String body = getRequest.getBody();
                JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
                if(!jsonArray.isEmpty()) {
                    for(int j=0;j<jsonArray.size();j++) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(j);
                            JSONObject filePonter = jsonObject.getJSONObject("filePointer");
                            String fileUrl = filePonter.getString("url");
                            if(fileUrl.startsWith("http://localhost:8080/parse")){
                                fileUrl = fileUrl.replace("http://localhost:8080/parse", Configuration.TXTSTREET_PARSE_URL);
                            } else if(fileUrl.startsWith("***REMOVED***")) {
                                fileUrl = fileUrl.replace("***REMOVED***", Configuration.TXTSTREET_PARSE_URL);
                            }
                            HttpRequest fileRequest = requestFactory.buildGetRequest(new GenericUrl(fileUrl));
                            com.google.api.client.http.HttpResponse fileRequestResponse = fileRequest.execute();
                            final ByteArrayOutputStream out = new ByteArrayOutputStream();
                            fileRequestResponse.download(out);
                            long fileSize = out.size();
                            String objectId = jsonObject.getString("objectId");
                            updateFileSize(objectId, fileSize);
                            totalFileSize = totalFileSize + fileSize;
                            LOG.info("File objectId: " + objectId);
                            LOG.info("File url: " + fileUrl);
                            LOG.info("File size: " + fileSize);
                            LOG.info("Progress " + i + "/" + (chops - 1));
                            LOG.info("Index " + j + "/" + jsonArray.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            setStatus(Status.SUCCESS_OK);
            result.put("success", true);
            result.put("count", count);
            result.put("size", totalFileSize);
            saveCalculatedSize(count, totalFileSize);
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
        GenericUrl url = new GenericUrl(Configuration.TXTSTREET_PARSE_URL + "/classes/File");
        url.put("limit", 0);
        url.put("count", 1);
        HttpRequest httpRequest = requestFactory.buildGetRequest(url);
        httpRequest.getHeaders().set("X-Parse-Application-Id", Configuration.TXTSTREET_PARSE_APP_ID);
        httpRequest.getHeaders().set("X-Parse-REST-API-Key", Configuration.TXTSTREET_PARSE_REST_API_KEY);
        httpRequest.getHeaders().set("X-Parse-Master-Key", Configuration.TXTSTREET_MASTER_KEY);
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

            com.mashape.unirest.http.HttpResponse<String> saveResponse = Unirest.post(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/TotalFile")
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
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

            com.mashape.unirest.http.HttpResponse<String> updateResponse = Unirest.put(Configuration.TXTSTREET_PARSE_URL +
                    "/classes/File/" + fileId)
                    .header(X_PARSE_APPLICATION_ID, Configuration.TXTSTREET_PARSE_APP_ID)
                    .header(X_PARSE_REST_API_KEY, Configuration.TXTSTREET_PARSE_REST_API_KEY)
                    .header(X_MASTER_KEY, Configuration.TXTSTREET_MASTER_KEY)
                    .header("Content-Type", "application/json")
                    .body(updateObject.toJSONString())
                    .asString();
            LOG.info("Update Status " + fileId + ": " + updateResponse.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
