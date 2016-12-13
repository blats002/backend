package com.divroll.core.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.divroll.core.rest.resource.gae.GaeRootServerResource;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
//import com.mashape.unirest.http.HttpResponse;
//import com.mashape.unirest.http.Unirest;
import com.google.common.io.CountingOutputStream;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class ParseFileRepresentation extends OutputRepresentation {

    private static final String ROOT_URI = "/";
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    final static Logger LOG
            = LoggerFactory.getLogger(ParseFileRepresentation.class);


    private String path = null;

    private String parseRestApiKey;

    private String parseAppId;

    private String parseBase;

    private String appId;

    public ParseFileRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    public ParseFileRepresentation(String appId, String path, String parseAppId, String parseRestApiKey, String parseBase, MediaType mediaType) {
        super(mediaType);
        setPath(path);
        setParseRestApiKey(parseRestApiKey);
        setParseAppId(parseAppId);
        setParseBase(parseBase);
        setAppId(appId);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        System.out.println("APP_ID: " + appId);
        System.out.println("REST_API_KEY: " + parseRestApiKey);
        String completePath = this.path;
        try {
            long numBytes = 0;
            Object cached = null;
            if(cached != null){
                outputStream.write((byte[]) cached);
                numBytes = ((byte[]) cached).length;
            } else {
                if(completePath.endsWith(ROOT_URI)) {
                    String jsonString = "NOT FOUND";
                    outputStream.write(jsonString.getBytes());
                } else {
                    // Check if app exists
                    System.out.println("File request path: " + path);
                    System.out.println("File metadata found: " + completePath);
                    HttpRequestFactory requestFactory =
                            HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                                @Override
                                public void initialize(HttpRequest request) {
                                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                                }
                            });
                    Map<String, String> json = new HashMap<String, String>();
                    json.put("path", path);
                    json.put("appId", appId);


                    GaeRootServerResource.ParseUrl url = new GaeRootServerResource.ParseUrl(parseBase + "/functions/file");
                    HttpRequest request = requestFactory.buildPostRequest(url, new JsonHttpContent(new JacksonFactory(), json));
                    request.getHeaders().set("X-Parse-Application-Id", parseAppId);
                    request.getHeaders().set("X-Parse-REST-API-Key", parseRestApiKey);
                    request.getHeaders().set("X-Parse-Revocable-Session", "1");
                    request.setRequestMethod("POST");

                    System.out.println("Parse Base URL: " + parseBase);

                    com.google.api.client.http.HttpResponse response = request.execute();
                    String body = new Scanner(response.getContent()).useDelimiter("\\A").next();
                    System.out.println("Function Response: " + body);

                    JSONObject jsonObject = JSON.parseObject(body);
                    String fileUrl = jsonObject.getJSONObject("result").getString("url");
                    System.out.println("File URL: " + fileUrl);

                    if(fileUrl.startsWith("http://localhost:8080/parse")){
                        fileUrl = fileUrl.replace("http://localhost:8080/parse", parseBase);
                    }

                    // Get the file and stream it
                    HttpRequest fileRequest = requestFactory.buildGetRequest(new GenericUrl(fileUrl));
                    com.google.api.client.http.HttpResponse fileRequestResponse = fileRequest.execute();
                    final CountingOutputStream countingOutputStream = new CountingOutputStream(outputStream);
                    fileRequestResponse.download(countingOutputStream);

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            modifyAppUsage(appId, countingOutputStream.getCount());
                        }
                    };
                    Thread t = new Thread(runnable);
                    t.run();

                }
            }
        }  catch (Exception e){
            e.printStackTrace();
            String error = "Error serving that request. Please try again.";
            outputStream.write(error.getBytes());
        }
        outputStream.close();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void getFile(String subdomain, String completePath, OutputStream outputStream) throws IOException {

    }

    public void setParseRestApiKey(String restApiKey) {
        this.parseRestApiKey = restApiKey;
    }

    public void setParseBase(String parseBase) {
        this.parseBase = parseBase;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getParseAppId() {
        return parseAppId;
    }

    public void setParseAppId(String parseAppId) {
        this.parseAppId = parseAppId;
    }

    private void modifyAppUsage(String appId, Long newBytes) {
        try {
            HttpRequestFactory requestFactory =
                    HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) {
                            request.setParser(new JsonObjectParser(JSON_FACTORY));
                        }
                    });
            Map<String, Object> json = new HashMap<String, Object>();
            json.put("appId", appId);
            json.put("bytes", newBytes);
            GaeRootServerResource.ParseUrl meterUrl = new GaeRootServerResource.ParseUrl(parseBase + "/functions/meter");
            HttpRequest meterRequest = requestFactory.buildPostRequest(meterUrl, new JsonHttpContent(new JacksonFactory(), json));
            meterRequest.getHeaders().set("X-Parse-Application-Id", parseAppId);
            meterRequest.getHeaders().set("X-Parse-REST-API-Key", parseRestApiKey);
            meterRequest.getHeaders().set("X-Parse-Revocable-Session", "1");
            meterRequest.setRequestMethod("POST");

            com.google.api.client.http.HttpResponse meterResponse = meterRequest.execute();
            String meterBody = new Scanner(meterResponse.getContent()).useDelimiter("\\A").next();
            LOG.debug("Meter Function Response: " + meterBody);

        } catch (Exception e) {
            LOG.debug("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
