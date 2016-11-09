/*
*
* Copyright (c) 2016 Kerby Martino and Divroll. All Rights Reserved.
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
package com.divroll.core.rest.resource.gae;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.divroll.core.rest.guice.SelfInjectingServerResource;
import com.divroll.core.rest.util.RegexHelper;
import com.divroll.core.rest.ParseFileRepresentation;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import com.alibaba.fastjson.JSON;
import org.slf4j.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Resource which has only one representation.
 */
public class GaeRootServerResource extends SelfInjectingServerResource {

    final static Logger LOG
            = LoggerFactory.getLogger(GaeRootServerResource.class);
    private static final String PARSE_APP_ID = "divroll";
    private static final String PARSE_REST_API_KEY = "enterKey";
    private static final String ROOT_URI = "";
    private static final String APP_ROOT_URI = "";
	private static final String KEY_SPACE = ":";
    private static final int EXPIRATION = 3600000; // 1 hour

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public static class ParseUrl extends GenericUrl {
        public ParseUrl(String encodedUrl) {
            super(encodedUrl);
        }
    }

    @Inject
    @Named("parse.url")
    private String parseUrl;

    @Inject
    @Named("app.domain")
    private String appDomain;

    @Inject
    @Named("app.domain.local")
    private String appDomainLocal;

    @Inject
    @Named("dropbox.token")
    private String dropboxToken;

    @Inject
    @Named("kinvey.appkey")
    private String appkey;

    @Inject
    @Named("kinvey.mastersecret")
    private String masterSecret;

    @Override
    protected void doInit() {
        super.doInit();
    }

    @Get
    public Representation represent() {
        Representation entity = null;
        MediaType type = getRequest().getEntity().getMediaType();
        String path = getRequest().getResourceRef().getPath();
        String _completePath = getRequest().getResourceRef().getHostIdentifier() +
                getRequest().getResourceRef().getPath();
        URL url = null;

        LOG.info("Request Path: " + path);

        try {
            url = new URL(_completePath);
            String host = url.getHost();

            String p = url.getPath();
            if(p.isEmpty() || p.equals("/")){
                p = "index.html";
            }else if(p.startsWith("/")){
                p = p.substring(1);
            }
            final String subdomain;
            subdomain = parseSubdomain(host);
            System.out.println("Application ID: " + subdomain);
            if(subdomain == null){
                String error = "404 NOT FOUND";
                entity = new StringRepresentation(error);
                entity.setMediaType(MediaType.TEXT_PLAIN);
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return entity;
            } else {
                p = p.replace("%20", " ");
                final String completePath = APP_ROOT_URI + p;

                System.out.println("Complete Path: " + completePath);
                System.out.println("Host: " + host);
                System.out.println("Application ID/Subdomain: " + subdomain);

                entity = new ParseFileRepresentation(subdomain, completePath, dropboxToken, parseUrl, type);
                entity.setMediaType(processMediaType(completePath));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
        return entity;
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
            GaeRootServerResource.ParseUrl url = new GaeRootServerResource.ParseUrl(parseUrl + "/classes/Domain");
            url.put("where", where.toJSONString());
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().set("X-Parse-Application-Id", PARSE_APP_ID);
            request.getHeaders().set("X-Parse-REST-API-Key", PARSE_REST_API_KEY);
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
            GaeRootServerResource.ParseUrl url = new GaeRootServerResource.ParseUrl(parseUrl + "/classes/Application");
            url.put("where", where.toJSONString());
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().set("X-Parse-Application-Id", PARSE_APP_ID);
            request.getHeaders().set("X-Parse-REST-API-Key", PARSE_REST_API_KEY);
            request.getHeaders().set("X-Parse-Revocable-Session", "1");
            request.setRequestMethod("GET");
            com.google.api.client.http.HttpResponse response = request.execute();
            String body = new Scanner(response.getContent()).useDelimiter("\\A").next();
            System.out.println("Get Application Name Response: " + body);

            JSONObject jsonBody = JSON.parseObject(body);
            JSONArray array = jsonBody.getJSONArray("results");
            JSONObject resultItem = (JSONObject) array.iterator().next();
            appName = resultItem.getString("appId");
        } catch (Exception e) {
            LOG.debug("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return appName;
    }


    private String parseSubdomain(String host){
        if(host.endsWith("divroll.com")){
            return RegexHelper.parseSubdomain(host, "divroll.com");
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
            ParseUrl url = new ParseUrl(parseUrl + "/classes/Application");
            url.put("where", where.toJSONString());
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().set("X-Parse-Application-Id", PARSE_APP_ID);
            request.getHeaders().set("X-Parse-REST-API-Key", PARSE_REST_API_KEY);
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

}
