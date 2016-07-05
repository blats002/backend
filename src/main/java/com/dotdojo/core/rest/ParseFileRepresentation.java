package com.divroll.core.rest;

import com.alibaba.fastjson.JSON;
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
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class ParseFileRepresentation extends OutputRepresentation {

    public static final String PARSE_URL = "***REMOVED***";
    private static final String KEY_SPACE = ":";
    private static final String ROOT_URI = "/";

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    final static Logger LOG
            = LoggerFactory.getLogger(ParseFileRepresentation.class);


    private String path = null;

    private String dropboxToken;

    private String parseBase;

    private String appId;

    public ParseFileRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    public ParseFileRepresentation(String appId, String path, String token, String parseBase, MediaType mediaType) {
        super(mediaType);
        setPath(path);
        setDropboxToken(token);
        setParseBase(parseBase);
        setAppId(appId);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
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
                    LOG.info("File request path: " + path);
                    LOG.debug("File metadata not found: " + completePath);
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
                    request.getHeaders().set("X-Parse-Application-Id", "hSeqDDBeLesJxWjaxAuHq34q3L7ws6xm3qoSS8yG");
                    request.getHeaders().set("X-Parse-REST-API-Key", "d4dVXvvL85TcG27dgXfHsCKzXcb4g7IZH7tQV9V7");
                    request.getHeaders().set("X-Parse-Revocable-Session", "1");
                    request.setRequestMethod("POST");

                    com.google.api.client.http.HttpResponse response = request.execute();
                    String body = new Scanner(response.getContent()).useDelimiter("\\A").next();
                    LOG.info("Function Response: " + body);

                    JSONObject jsonObject = JSON.parseObject(body);
                    String fileUrl = jsonObject.getJSONObject("result").getString("url");
                    LOG.info("File URL: " + fileUrl);

                    // Get the file and stream it
                    HttpRequest fileRequest = requestFactory.buildGetRequest(new GenericUrl(fileUrl));
                    com.google.api.client.http.HttpResponse fileRequestResponse = fileRequest.execute();
                    fileRequestResponse.download(outputStream);
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

    public void setDropboxToken(String dropboxToken) {
        this.dropboxToken = dropboxToken;
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
}
