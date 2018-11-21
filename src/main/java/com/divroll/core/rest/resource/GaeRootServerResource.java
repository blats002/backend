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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.divroll.backend.Divroll;
import com.divroll.backend.DivrollEntities;
import com.divroll.backend.DivrollEntity;
import com.divroll.backend.filter.EqualQueryFilter;
import com.divroll.core.rest.CloudFileRepresentation;
import com.divroll.core.rest.WasabiFileRepresentation;
import com.divroll.core.rest.service.CacheService;
import com.divroll.core.rest.util.RegexHelper;
import com.divroll.core.rest.util.StringUtil;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.Inject;
import com.mashape.unirest.http.Unirest;
import org.restlet.data.*;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 *
 * Resource which has only one representation.
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class GaeRootServerResource extends BaseServerResource {

    final static Logger LOG
            = LoggerFactory.getLogger(GaeRootServerResource.class);

    private static final String HASH = "#";
    private static final String APP_ROOT_URI = "";
    private static final String ESCAPED_FRAGMENT_FORMAT = "_escaped_fragment_";
    private static final String ESCAPED_FRAGMENT_FORMAT1 = "_escaped_fragment_=";
    private static final int ESCAPED_FRAGMENT_LENGTH1 = ESCAPED_FRAGMENT_FORMAT1.length();
    private static final int YEAR_IN_MINUTES = 365 * 24 * 60 * 60;

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private String acceptEncodings;
    private String cacheKey;

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
        cacheKey = getQueryValue("cachekey");
        Divroll.initialize(serverUrl, appId, appKey, masterKey);
    }

    @Delete
    public Representation delete() {
        EncodeRepresentation encoded = null;
        try {
            if(cacheKey != null && !cacheKey.isEmpty()) {
                cacheService.delete(cacheKey);
                setStatus(Status.SUCCESS_OK);
                encoded = new EncodeRepresentation(Encoding.GZIP, new StringRepresentation("OK"));
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            e.printStackTrace();
        }
        return encoded;
    }

    @SuppressWarnings("unused")
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
            String escapeQuery = getQueryValue(ESCAPED_FRAGMENT_FORMAT);
            Form queries = getQuery();
            if(escapeQuery != null && !escapeQuery.isEmpty()) {
                String decodedFragment = URLDecoder.decode(escapeQuery, "UTF-8");
                com.mashape.unirest.http.HttpResponse<String> response = Unirest.get(getPrerenderUrl())
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
            } else if(queries != null && !queries.isEmpty() && queries.getValues("f") != null) {
                // TODO: just a quick
                String escapedFragment = queries.getValues("f");
                if(_completePath.endsWith("/")) {
                    _completePath = _completePath.substring(0, _completePath.length() - 1);
                }
                String getPath = getPrerenderUrl() + "?url=" + _completePath + "&_escaped_fragment_=" + escapedFragment;
                LOG.info("GET Path=" + getPath);
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
                String subdomain = null;
                if(host.equals("divroll.com")) {
                    subdomain = "www";
                } else {
                    subdomain = parseSubdomain(host);
                }

                LOG.info("Application ID: " + subdomain);
                if( (subdomain == null || subdomain.isEmpty()) || !isValidSubdomain(subdomain)){
                    //subdomain = "404";
                    Representation responseEntity = new StringRepresentation(read404template());
                    responseEntity.setMediaType(MediaType.TEXT_HTML);
                    setStatus(Status.SUCCESS_OK);
                    if(!canAcceptGzip) {
                        return responseEntity;
                    }
                    encoded = new EncodeRepresentation(Encoding.GZIP, responseEntity);
                    return encoded;
                }
                p = p.replace("%20", " ");
                final String completePath = APP_ROOT_URI + p;

                LOG.info("Complete Path:            " + completePath);
                LOG.info("Host:                     " + host);
                LOG.info("Application ID/Subdomain: " + subdomain);

                //JSONObject postObject = new JSONObject();
                //postObject.put("path", p);
                //postObject.put("appId", subdomain);

                ////////////////////////////////////////////////////////////////////////////////////////////////////
                // Main code that reads file from cache or Cloud Storage
                ////////////////////////////////////////////////////////////////////////////////////////////////////
                String completeFilePath = subdomain + "/" + p;
                LOG.info("Complete File Path:       " + completeFilePath);
                byte[] cachedBytes = null;
                try{
                    cachedBytes = cacheService.get(completeFilePath);
                } catch (Exception e) {
                }
                Representation responseEntity = null;
                if(cachedBytes != null) {
                    responseEntity = new ByteArrayRepresentation(cachedBytes);
                    responseEntity.setMediaType(processMediaType(completePath));
                } else {
                    responseEntity = new WasabiFileRepresentation(
                            completeFilePath,
                            processMediaType(path),
                            cacheService);
                    responseEntity.setMediaType(processMediaType(completePath));
                }
                if(!canAcceptGzip) {
                    return responseEntity;
                }
                encoded = new EncodeRepresentation(Encoding.GZIP, responseEntity);
                return encoded;
                ////////////////////////////////////////////////////////////////////////////////////////////////////
            }
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            e.printStackTrace();
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            if(e instanceof FileNotFoundException) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                setStatus(Status.SERVER_ERROR_INTERNAL);
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
        } else if (path.endsWith("svg")) {
            type = MediaType.IMAGE_SVG;
        } else if (path.endsWith("ico")){
            type = MediaType.IMAGE_ICON;
        } else if (path.endsWith("ogg")) {
            type = MediaType.valueOf("audio/ogg");
        }
        return type;
    }

    private boolean isValidSubdomain(String subdomain) {
        final Boolean[] isValid = {false};
        DivrollEntities entities = new DivrollEntities("Subdomain");
        entities.query(new EqualQueryFilter("subdomain", subdomain));
        entities.getEntities().forEach(divrollEntity -> {
            String subDomain = String.valueOf(divrollEntity.getProperty("subdomain"));
            if(subDomain != null && subDomain.equals(subDomain)) {
                isValid[0] = true;
            }
        });
        return isValid[0];
    }

    // TODO: Convert to cloud code
    private String getStoredSubdomain(String host){
        LOG.info("Get stored subdomain: " + host);
        String result = null;
        try{
            result = cacheService.getString("domain:" + host + ":subdomain");
        } catch (Exception e) {
        }
        if(result != null) {
            return result;
        }
        try {
            String domain = null;
            DivrollEntities entities = new DivrollEntities("Domain");
            entities.query(new EqualQueryFilter("name", host));
            final DivrollEntity[] entity = new DivrollEntity[1];
            entities.getEntities().forEach(divrollEntity -> {
                entity[0] = divrollEntity;
            });
            if(entity[0] != null) {
                domain = String.valueOf(entity[0].getProperty("name"));
                List<DivrollEntity> linked = entity[0].links("subdomain");
                final String[] finalSubdomain = new String[1];
                linked.forEach(divrollEntity -> {
                    finalSubdomain[0] = String.valueOf(divrollEntity.getProperty("subdomain"));
                });
                result = finalSubdomain[0];
                if(domain != null && result != null) {
                    String cacheKey = "domain:" + host + ":subdomain" + "->" + result;
                    cacheService.putString(cacheKey, result);
                }
            }
//            System.out.println("HOST: " + host);
//            System.out.println("domain: " + domain);
//            System.out.println("subdomain: " + result);
        } catch (Exception e) {
            LOG.debug("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @Deprecated
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
            GaeRootServerResource.ParseUrl url = new GaeRootServerResource.ParseUrl(getParseServerUrl() + "/classes/Application");
            url.put("where", where.toJSONString());
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().set("X-Parse-Application-Id", getParseAppId());
            request.getHeaders().set("X-Parse-REST-API-Key", getParseRestAPIkey());
            request.getHeaders().set("X-Parse-Revocable-Session", "1");
            request.setRequestMethod("GET");
            com.google.api.client.http.HttpResponse response = request.execute();
            String body = new Scanner(response.getContent()).useDelimiter("\\A").next();
            LOG.info("Get Application Name Response: " + body);

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

    @Deprecated
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
            ParseUrl url = new ParseUrl(getParseServerUrl() + "/classes/Application");
            url.put("where", where.toJSONString());
            HttpRequest request = requestFactory.buildGetRequest(url);
            request.getHeaders().set("X-Parse-Application-Id", getParseAppId());
            request.getHeaders().set("X-Parse-REST-API-Key", getParseRestAPIkey());
            request.getHeaders().set("X-Parse-Revocable-Session", "1");
            request.setRequestMethod("GET");

            HttpResponse response = request.execute();
            String body = new Scanner(response.getContent()).useDelimiter("\\A").next();

            //LOG.info("Get appId Response: " + body);

            JSONArray resultsArray = JSON.parseObject(body)
                    .getJSONArray("results");

            if(!resultsArray.isEmpty()){
                LOG.info("Result: " + body);
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

    protected String read404template(){
        InputStream is = this.getClass().getResourceAsStream("/error404.html");
        return StringUtil.toString(is);
    }

}
