/*
*
* Copyright (c) 2017 Kerby Martino and Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   https://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/
package com.divroll.core.rest.resource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.divroll.core.rest.Config;
import com.divroll.core.rest.exception.FileNotFoundException;
import com.divroll.core.rest.util.RegexHelper;
import com.divroll.core.rest.ParseFileRepresentation;
import com.google.api.client.http.*;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.mashape.unirest.http.*;
import org.restlet.data.*;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import com.alibaba.fastjson.JSON;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;
import org.slf4j.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 *
 * Resource which has only one representation.
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class GaeRootServerResource extends ServerResource {

    final static Logger LOG
            = LoggerFactory.getLogger(GaeRootServerResource.class);

//    private static final String PRERENDER_URL = "***REMOVED***";
    private static final String PRERENDER_URL = "***REMOVED***";
    private static final String HASH = "#";
    private static final String ESCAPED_FRAGMENT_FORMAT1 = "_escaped_fragment_=";
    private static final int ESCAPED_FRAGMENT_LENGTH1 = ESCAPED_FRAGMENT_FORMAT1.length();
    private static final String APP_ROOT_URI = "";

    public static final int YEAR_IN_MINUTES = 365 * 24 * 60 * 60;

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private String acceptEncodings;

    public static class ParseUrl extends GenericUrl {
        public ParseUrl(String encodedUrl) {
            super(encodedUrl);
        }
    }

    @Override
    protected void doInit() {
        super.doInit();
        Series<Header> series = (Series<Header>)getRequestAttributes().get("org.restlet.http.headers");
        acceptEncodings =  series.getFirst("Accept-Encoding") != null ? series.getFirst("Accept-Encoding").getValue() : "";
    }

    @Get
    public Representation represent() {
        boolean canAcceptGzip = (acceptEncodings.contains("gzip") || acceptEncodings.contains("GZIP"));
        EncodeRepresentation encoded = null;
        MediaType type = getRequest().getEntity().getMediaType();
        String path = getRequest().getResourceRef().getPath();
        String _completePath = getRequest().getResourceRef().getHostIdentifier() +
                getRequest().getResourceRef().getPath();
        URL url = null;
        LOG.info("Request Path: " + path);

        try {
            String escapeQuery = getQueryValue("_escaped_fragment_");
            Form queries = getQuery();
            if(escapeQuery != null && !escapeQuery.isEmpty()) {
                String decodedFragment = URLDecoder.decode(escapeQuery, "UTF-8");
                com.mashape.unirest.http.HttpResponse<String> response = Unirest.get(PRERENDER_URL)
                        .queryString("url", _completePath)
                        .queryString("_escaped_fragment_", decodedFragment)
                        .asString();
                String body = response.getBody();
                Representation entity = new StringRepresentation(body);
                //entity.setMediaType(processMediaType(s));
                entity.setMediaType(MediaType.TEXT_HTML);
                if(!canAcceptGzip) {
                    return entity;
                }
                encoded = new EncodeRepresentation(Encoding.GZIP, entity);
            }else if(queries != null && !queries.isEmpty() && queries.getValues("f") != null) { // TODO: just a quick
                String escapedFragment = queries.getValues("f");
                    if(_completePath.endsWith("/")) {
                        _completePath = _completePath.substring(0, _completePath.length() - 1);
                    }
                    String getPath = PRERENDER_URL + "?url=" + _completePath + "&_escaped_fragment_=" + escapedFragment;
                    System.out.println("GET Path=" + getPath);
                    com.mashape.unirest.http.HttpResponse<String> response = Unirest.get(getPath)
                            .asString();
                    String body = response.getBody();
                    Representation entity = new StringRepresentation(body);
                    //entity.setMediaType(processMediaType(s));
                    entity.setMediaType(MediaType.TEXT_HTML);
                    if(!canAcceptGzip) {
                        return entity;
                    }
                    encoded = new EncodeRepresentation(Encoding.GZIP, entity);
            } else {
                url = new URL(_completePath);
                String host = url.getHost();

                String p = url.getPath();
                if(p.isEmpty() || p.equals("/")){
                    p = "index.html";
                }else if(p.startsWith("/")){
                    p = p.substring(1);
                }
                String subdomain;
                subdomain = parseSubdomain(host);
                System.out.println("Application ID: " + subdomain);
                if(subdomain == null || subdomain.isEmpty()){
                    subdomain = "404";
                } else {
                    p = p.replace("%20", " ");
                    final String completePath = APP_ROOT_URI + p;

                    System.out.println("Complete Path: " + completePath);
                    System.out.println("Host: " + host);
                    System.out.println("Application ID/Subdomain: " + subdomain);

                    JSONObject postOobject = new JSONObject();
                    postOobject.put("path", p);
                    postOobject.put("appId", subdomain);

                    String appObjectId = null;

                    // Get objectId give subdomain
                    JSONObject whereObject = new JSONObject();
                    whereObject.put("appId", subdomain);
                    com.mashape.unirest.http.HttpResponse<String> quotaRequest = Unirest.get(Config.PARSE_URL +
                            "/classes/Application")
                            .header("X-Parse-Application-Id", Config.PARSE_APP_ID)
                            .header("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY)
                            .header("X-Parse-Revocable-Session", "1")
                            .queryString("where", whereObject.toJSONString())
                            .asString();
                    String body = quotaRequest.getBody();
                    System.out.println("Body: " + body);
                    JSONArray jsonArray = JSONObject.parseObject(body).getJSONArray("results");
                    if(!jsonArray.isEmpty()) {
                        for(int i=0;i<jsonArray.size();i++){
                            JSONObject item = jsonArray.getJSONObject(i);
                            if(item.getString("appId") != null && item.getString("appId").equals(subdomain)) {
                                appObjectId = item.getString("objectId");
                            }

                        }
                    }

                    if(appObjectId == null) {
                        System.out.println("Application objectId is null for " + subdomain);
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return null;
                    }
                    System.out.println("Application ID: " + appObjectId);

                    HttpRequestFactory requestFactory =
                            HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                                @Override
                                public void initialize(HttpRequest request) {
                                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                                }
                            });

                    // Get the file given Application objectId and path
                    JSONObject getFileWhereObject = new JSONObject();
                    getFileWhereObject.put("appId", createPointer("Application", appObjectId));
                    getFileWhereObject.put("path", p);
                    com.mashape.unirest.http.HttpResponse<String> getFileResponse = Unirest.get(Config.PARSE_URL +
                            "/classes/File")
                            .header("X-Parse-Application-Id", Config.PARSE_APP_ID)
                            .header("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY)
                            .header("X-Parse-Revocable-Session", "1")
                            .header("Content-Type", "application/json")
                            .queryString("where", getFileWhereObject.toJSONString())
                            .asString();
                    String getFileBody = getFileResponse.getBody();
                    JSONObject getFileJSONObject = JSONObject.parseObject(getFileBody);
                    JSONArray fileJsonArray = getFileJSONObject.getJSONArray("results");
                    if(!fileJsonArray.isEmpty()) {
                        for(int j=0;j<fileJsonArray.size();j++) {
                            try {
                                JSONObject jsonObject = fileJsonArray.getJSONObject(j);
                                JSONObject filePonter = jsonObject.getJSONObject("filePointer");
                                String fileUrl = filePonter.getString("url");
                                if(fileUrl.startsWith("http://localhost:8080/parse")){
                                    fileUrl = fileUrl.replace("http://localhost:8080/parse", Config.PARSE_URL);
                                } else if(fileUrl.startsWith(Config.PARSE_PUBLIC_URL)) {
                                    fileUrl = fileUrl.replace(Config.PARSE_PUBLIC_URL, Config.PARSE_URL);
                                }
                                Representation entity = new ParseFileRepresentation(Config.PARSE_URL,
                                        Config.PARSE_APP_ID,
                                        Config.PARSE_REST_API_KEY,
                                        fileUrl,
                                        processMediaType(fileUrl));
                                entity.setMediaType(processMediaType(completePath));
                                if(!canAcceptGzip) {
                                    return entity;
                                }
                                encoded = new EncodeRepresentation(Encoding.GZIP, entity);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    /*
                    com.mashape.unirest.http.HttpResponse<String> postResponse = null;
                    postResponse = Unirest.post(Config.PARSE_URL + "/functions/file")
                                .header("X-Parse-Application-Id", Config.PARSE_APP_ID)
                                .header("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY)
                                .header("X-Parse-Revocable-Session", "1")
                                .body(postOobject.toJSONString())
                                .asString();
                        System.out.println("========================================================================================");
                        System.out.println("Function Response: " + postResponse.getBody());
                        System.out.println("========================================================================================");

                        JSONObject jsonObject = JSON.parseObject(postResponse.getBody());

                        if(jsonObject.get("error") != null
                                && jsonObject.get("code") != null) {
                             throw new FileNotFoundException(jsonObject.getString("error"));
                        } else {
                            String fileUrl = jsonObject.getJSONObject("result").getString("url");
                            System.out.println("File URL: " + fileUrl);
                            if(fileUrl == null || fileUrl.isEmpty()) {
                                throw new FileNotFoundException();
                            }
                            Representation entity = new ParseFileRepresentation(Config.PARSE_URL,
                                    Config.PARSE_APP_ID,
                                    Config.PARSE_REST_API_KEY,
                                    fileUrl,
                                    processMediaType(fileUrl));
                            entity.setMediaType(processMediaType(completePath));
                            if(!canAcceptGzip) {
                                return entity;
                            }
                            encoded = new EncodeRepresentation(Encoding.GZIP, entity);
                        }
                    */
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            if(e instanceof FileNotFoundException) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
            if(e instanceof FileNotFoundException) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }
        }
        /*
        GWT Headers
         */
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series(Header.class);
        }
        if(_completePath.contains(".nocache.")) {
            Date now = new Date();
            responseHeaders.set("Date", String.valueOf(now.getTime()));
            responseHeaders.set("Last-Modified", String.valueOf(now.getTime()));
            responseHeaders.set("Expires", "0");
            responseHeaders.set("Pragma", "no-cache");
            responseHeaders.set("Cache-control", "no-cache, must-revalidate, pre-check=0, post-check=0");
        } else if(_completePath.contains(".cache.")) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime( new Date() );
            calendar.add( Calendar.YEAR, 1 );
            responseHeaders.set("Expires", String.valueOf(calendar.getTime().getTime()));
            //Note: immutable tells firefox to never revalidate as data will never change
            responseHeaders.set( "Cache-control", "max-age=" + YEAR_IN_MINUTES + ", public, immutable" );
            responseHeaders.set( "Pragma", "" );
        }
        getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        return encoded;
    }

    private MediaType processMediaType(String path){
        MediaType type = MediaType.ALL;
        if(path.endsWith("html")){
            type = MediaType.TEXT_HTML;
        } else if (path.endsWith("css")) {
            type = MediaType.TEXT_CSS;
        } else if (path.endsWith("js")) {
            type = MediaType.TEXT_JAVASCRIPT;
        } else if (path.endsWith("txt")) {
            type = MediaType.TEXT_PLAIN;
        } else if (path.endsWith("jpg")){
            type = MediaType.IMAGE_JPEG;
        } else if (path.endsWith("png")){
            type = MediaType.IMAGE_PNG;
        } else if (path.endsWith("ico")){
            type = MediaType.IMAGE_ICON;
        }
        return type;
    }

    // TODO: Convert to cloud code
    private String getStoredSubdomain(String host){
        LOG.info("Get stored subdomain: " + host);
        String result = null;
        try {
            HttpRequestFactory requestFactory =
                    HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) {
                            request.setParser(new JsonObjectParser(JSON_FACTORY));
                        }
                    });
            JSONObject where = new JSONObject();
            where.put("name", host);
            GaeRootServerResource.ParseUrl url = new GaeRootServerResource.ParseUrl(Config.PARSE_URL + "/classes/Domain");
            url.put("where", where.toJSONString());
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().set("X-Parse-Application-Id", Config.PARSE_APP_ID);
            request.getHeaders().set("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY);
            request.getHeaders().set("X-Parse-Revocable-Session", "1");
            request.setRequestMethod("GET");
            com.google.api.client.http.HttpResponse response = request.execute();
            String body = new Scanner(response.getContent()).useDelimiter("\\A").next();
            System.out.println("Response: " + body);
            JSONObject jsonBody = JSON.parseObject(body);
            JSONArray array = jsonBody.getJSONArray("results");
            JSONObject resultItem = (JSONObject) array.iterator().next();
            JSONObject appPointer = resultItem.getJSONObject("appId");
            result = getApplicationName(appPointer);
        } catch (Exception e) {
            LOG.debug("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private String getApplicationName(JSONObject pointer) {
        String appName = null;
        try {
            String objectId = pointer.getString("objectId");
            HttpRequestFactory requestFactory =
                    HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) {
                            request.setParser(new JsonObjectParser(JSON_FACTORY));
                        }
                    });
            JSONObject where = new JSONObject();
            where.put("objectId", objectId);
            GaeRootServerResource.ParseUrl url = new GaeRootServerResource.ParseUrl(Config.PARSE_URL + "/classes/Application");
            url.put("where", where.toJSONString());
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().set("X-Parse-Application-Id", Config.PARSE_APP_ID);
            request.getHeaders().set("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY);
            request.getHeaders().set("X-Parse-Revocable-Session", "1");
            request.setRequestMethod("GET");
            com.google.api.client.http.HttpResponse response = request.execute();
            String body = new Scanner(response.getContent()).useDelimiter("\\A").next();
            System.out.println("Get Application Name Response: " + body);

            JSONObject jsonBody = JSON.parseObject(body);
            JSONArray array = jsonBody.getJSONArray("results");
            JSONObject resultItem = (JSONObject) array.iterator().next();
            appName = resultItem.getString("appId");
            /*
            Boolean isActive = resultItem.getBoolean("isActive");
            try {
                if(isActive) {
                    appName = resultItem.getString("appId");
                } else {
                    appName = "404";
                }
            } catch (Exception e){
                e.printStackTrace();
                appName = "404";
            }
            */
        } catch (Exception e) {
            LOG.debug("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return appName;
    }

    private String parseSubdomain(String host){
        if(host.endsWith("divroll.com")){
            return RegexHelper.parseSubdomain(host, "divroll.com");
        } else if(host.endsWith("localhost.com")){
            return RegexHelper.parseSubdomain(host, "localhost.com");
        } else {
            return getStoredSubdomain(host);
        }
    }

    private boolean isExist(String subdomain){
        try {
            JSONObject where = new JSONObject();
            where.put("appId", subdomain);
            HttpRequestFactory requestFactory =
                    HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) {
                            request.setParser(new JsonObjectParser(JSON_FACTORY));
                        }
                    });
            ParseUrl url = new ParseUrl(Config.PARSE_URL + "/classes/Application");
            url.put("where", where.toJSONString());
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().set("X-Parse-Application-Id", Config.PARSE_APP_ID);
            request.getHeaders().set("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY);
            request.getHeaders().set("X-Parse-Revocable-Session", "1");
            request.setRequestMethod("GET");

            HttpResponse response = request.execute();
            String body = new Scanner(response.getContent()).useDelimiter("\\A").next();

            //LOG.info("Get appId Response: " + body);

            JSONArray resultsArray = JSON.parseObject(body)
                    .getJSONArray("results");

            if(!resultsArray.isEmpty()){
                System.out.println("Result: " + body);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected JSONObject createPointer(String className, String objectId) {
        JSONObject pointer = new JSONObject();
        pointer.put("__type", "Pointer");
        pointer.put("className", className);
        pointer.put("objectId", objectId);
        return pointer;
    }
}
