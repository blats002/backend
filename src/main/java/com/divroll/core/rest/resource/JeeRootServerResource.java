/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.core.rest.resource;

import com.divroll.backend.util.RegexHelper;
import com.divroll.backend.util.StringUtil;
import com.divroll.core.rest.DivrollFileRepresentation;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.restlet.data.*;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.util.Series;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;

public class JeeRootServerResource extends BaseServerResource {

    final static Logger LOG
            = LoggerFactory.getLogger(JeeRootServerResource.class);

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
    private String environment;
    private boolean canAcceptGzip;

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
        environment = getQueryValue("environment");
        canAcceptGzip = (acceptEncodings.contains("gzip") || acceptEncodings.contains("GZIP"));

        String envServerUrl = System.getenv("DIVROLL_SERVER_URL");
        if(envServerUrl != null && !envServerUrl.isEmpty()) {
            serverUrl = envServerUrl;
        }
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
        canAcceptGzip = (acceptEncodings.contains("gzip") || acceptEncodings.contains("GZIP"));
        Representation encoded = null;
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
                String body = prerenderService.prerender(_completePath, decodedFragment);
                Representation entity = new StringRepresentation(body);
                //entity.setMediaType(processMediaType(s));
                entity.setMediaType(MediaType.TEXT_HTML);
                if(!canAcceptGzip) {
                    encoded = entity;
                } else {
                    encoded = new EncodeRepresentation(Encoding.GZIP, entity);
                }
            } else if(queries != null && !queries.isEmpty() && queries.getValues("f") != null) {
                // TODO: just a quick
                String escapedFragment = queries.getValues("f");
                if(_completePath.endsWith("/")) {
                    _completePath = _completePath.substring(0, _completePath.length() - 1);
                }
                String body = prerenderService.prerender(_completePath, escapedFragment);
                Representation entity = new StringRepresentation(body);
                //entity.setMediaType(processMediaType(s));
                entity.setMediaType(MediaType.TEXT_HTML);
                if(!canAcceptGzip) {
                    encoded = entity;
                } else {
                    encoded = new EncodeRepresentation(Encoding.GZIP, entity);
                }
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

                if(subdomain == null) {
                    subdomain = cacheService.getString(host + ":appName");
                    HttpResponse<JsonNode> response = Unirest.get(serverUrl + "/domains")
                            .header("X-Divroll-Master-Token", masterToken)
                            .queryString("domainName", host)
                            .asJson();
                    if(response.getStatus() == 202) {
                        JsonNode body = response.getBody();
                        JSONObject bodyObject = body.getObject();
                        JSONObject domainObject = bodyObject.getJSONObject("domain");
                        subdomain = domainObject.getString("appName");
                        LOG.info("App Name: " + subdomain);
                        cacheService.putString(host + ":appName", subdomain);
                    }
                }

                LOG.info("Application ID: " + subdomain);
                if( (subdomain == null || subdomain.isEmpty()) || !isValidSubdomain(subdomain)){
                    //subdomain = "404";
                    Representation responseEntity = new StringRepresentation(read404template());
                    responseEntity.setMediaType(MediaType.TEXT_HTML);
                    setStatus(Status.SUCCESS_OK);
                    if(!canAcceptGzip) {
                        encoded = responseEntity;
                    } else {
                        encoded = new EncodeRepresentation(Encoding.GZIP, responseEntity);
                    }
                }
                p = p.replace("%20", " ");
                final String completePath = APP_ROOT_URI + p;

                LOG.info("Complete Path:            " + completePath);
                LOG.info("Host:                     " + host);
                LOG.info("Application ID/Subdomain: " + subdomain);

                ////////////////////////////////////////////////////////////////////////////////////////////////////
                // Main code that reads file from cache or Cloud Storage
                ////////////////////////////////////////////////////////////////////////////////////////////////////

                if(environment != null) {
                    subdomain = subdomain + "." + environment;
                }

                //String completeFilePath = subdomain + "/" + p;
                String completeFilePath = p;
                LOG.info("Complete File Path:       " + completeFilePath);

                if(RegexHelper.isPath(completeFilePath)) {
                    String ref = RegexHelper.getRef(completePath);
                    setLocationRef(completePath + "index.html");
                    setStatus(Status.REDIRECTION_FOUND);
                    return null;
                }
                String appName = subdomain;
                String domainName = host;
                Representation responseEntity = new DivrollFileRepresentation(
                        masterToken,
                        appName,
                        domainName,
                        serverUrl,
                        completeFilePath,
                        processMediaType(path),
                        cacheService);
                responseEntity.setMediaType(processMediaType(completePath));
                if(!canAcceptGzip) {
                    encoded = responseEntity;
                } else {
                    encoded = new EncodeRepresentation(Encoding.GZIP, responseEntity);
                }
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
        String isValidCached = cacheService.getString("appName:" + subdomain + ":valid");
        // TODO: Add cache expiration (either here or to redis)
        if(isValidCached != null && isValidCached.equals("true")) {
            return true;
        }
        try {
            String completeUrl = serverUrl + APPLICATION_BASE_URI + subdomain;
            com.mashape.unirest.http.HttpResponse<String> response
                    = Unirest.get(completeUrl)
                        .header("X-Divroll-Master-Token", masterToken)
                        .asString();
            if(response.getStatus() == 204) {
                isValid[0] = true;
                cacheService.putString("appName:" + subdomain + ":valid", "true");
            } else if(response.getStatus() == 404) {
                cacheService.putString("appName:" + subdomain + ":valid", "false");
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return isValid[0];
    }

    private String parseSubdomain(String host){
        if(host.endsWith("divroll.com")){
            return RegexHelper.parseSubdomain(host, "divroll.com");
        } else if(host.endsWith("localhost.com")){
            return RegexHelper.parseSubdomain(host, "localhost.com");
        } else {
            //return getStoredSubdomain(host);
        }
        return null;
    }

    protected String read404template(){
        InputStream is = this.getClass().getResourceAsStream("/error404.html");
        return StringUtil.toString(is);
    }

}
