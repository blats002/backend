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
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * GPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.resource.jee;

import com.divroll.backend.hosting.DivrollFileRepresentation;
import com.divroll.backend.model.Application;
import com.divroll.backend.repository.FileRepository;
import com.divroll.backend.service.PrerenderService;
import com.divroll.backend.service.SubdomainService;
import com.divroll.backend.util.RegexHelper;
import com.divroll.backend.util.StringUtil;
import com.divroll.backend.xodus.XodusVFS;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import org.restlet.data.*;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.util.Series;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;

public class JeeSiteServerResource extends BaseServerResource {

    final static Logger LOG
            = LoggerFactory.getLogger(JeeSiteServerResource.class);

    private static final String HASH = "#";
    private static final String APP_ROOT_URI = "";
    private static final String ESCAPED_FRAGMENT_FORMAT = "_escaped_fragment_";
    private static final String ESCAPED_FRAGMENT_FORMAT1 = "_escaped_fragment_=";
    private static final int ESCAPED_FRAGMENT_LENGTH1 = ESCAPED_FRAGMENT_FORMAT1.length();
    private static final int YEAR_IN_MINUTES = 365 * 24 * 60 * 60;
    private static final String BASE_HOST = "divroll.com";
    private static final String BASE_HOST_PREFIX = "www";

    private String acceptEncodings;
    private String cacheKey;
    private String environment;
    private String hostDomain;
    private String requestPath;

    @Inject
    PrerenderService prerenderService;

//    @Inject
//    SubdomainService subdomainService;

    @Inject
    FileRepository fileRepository;

    @Inject
    XodusVFS vfs;

    @Override
    protected void doInit() {
        super.doInit();
        Series<Header> series = (Series<Header>)getRequestAttributes().get("org.restlet.http.headers");
        acceptEncodings =  series.getFirst("Accept-Encoding") != null ? series.getFirst("Accept-Encoding").getValue() : "";
        cacheKey = getQueryValue("cachekey");
        environment = getQueryValue("environment");
        hostDomain = getRequest().getResourceRef().getHostDomain();
        requestPath = getRequest().getResourceRef().getPath();
    }

    @Delete
    public Representation delete() {
        return badRequest();
    }

    @Put
    public Representation put() {
        return badRequest();
    }

    @Post
    public Representation post() {
        return badRequest();
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
                String body = prerenderService.prerender(_completePath, decodedFragment);
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
                LOG.info("GET Prerender URL=" + _completePath);
                LOG.info("GET Escaped Fragment=" + escapedFragment);
                String body = prerenderService.prerender(_completePath, escapedFragment);
                Representation entity = new StringRepresentation(body);
                //entity.setMediaType(processMediaType(s));
                entity.setMediaType(MediaType.TEXT_HTML);
                if(!canAcceptGzip) {
                    return entity;
                }
                encoded = new EncodeRepresentation(Encoding.GZIP, entity);
            } else {
                url = new URL(_completePath);

                if(requestPath == null || requestPath.isEmpty() || requestPath.equals("/")){
                    requestPath = "index.html";
                }else if(requestPath.startsWith("/")){
                    requestPath = requestPath.substring(1);
                }

                String appName = null;
                if(hostDomain.equals(BASE_HOST)) {
                    appName = BASE_HOST_PREFIX;
                } else {
                    appName = parseSubdomain(hostDomain);
                }

                Application application = null;
                if(appName != null) {
                    application = getAppByName(appName);
                } else {
                    application = getAppByDomain(hostDomain);
                }

                if(application == null) {
                    Representation responseEntity = new StringRepresentation(read404template());
                    responseEntity.setMediaType(MediaType.TEXT_HTML);
                    setStatus(Status.SUCCESS_OK);
                    if(!canAcceptGzip) {
                        return responseEntity;
                    }
                    encoded = new EncodeRepresentation(Encoding.GZIP, responseEntity);
                    return encoded;
                }

                LOG.info("Application ID: " + application.getAppName());

                requestPath = requestPath.replace("%20", " ");
                final String completePath = APP_ROOT_URI + requestPath;

                LOG.info("Complete Path:            " + completePath);
                LOG.info("Host:                     " + hostDomain);
                LOG.info("Application ID/Subdomain: " + appName);


                ////////////////////////////////////////////////////////////////////////////////////////////////////
                // Main code that reads file from Divroll Files
                ////////////////////////////////////////////////////////////////////////////////////////////////////

                if(environment != null) {
                    appName = appName + "." + environment;
                }

                //String completeFilePath = subdomain + "/" + p;
                //LOG.info("Complete File Path:       " + completeFilePath);

//                if(RegexHelper.isPath(completeFilePath)) {
//                    String ref = RegexHelper.getRef(completePath);
//                    setLocationRef(completePath + "/index.html");
//                    setStatus(Status.REDIRECTION_FOUND);
//                    return null;
//                }

                String appId = application.getAppId();

                Representation responseEntity = new DivrollFileRepresentation(
                        masterToken,
                        appId,
                        requestPath,
                        processMediaType(path),
                        cacheService, fileRepository);
                responseEntity.setMediaType(processMediaType(completePath));
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

    private String parseSubdomain(String host){
        if(host.endsWith("divroll.com")){
            return RegexHelper.parseSubdomain(host, "divroll.com");
        } else if(host.endsWith("localhost.com")){
            return RegexHelper.parseSubdomain(host, "localhost.com");
        } else {
            Application application = getAppByDomain(host);
            if(application != null) {
                return application.getAppName();
            } else {
                return null;
            }
        }
    }

    protected String read404template(){
        InputStream is = this.getClass().getResourceAsStream("/error404.html");
        return StringUtil.toString(is);
    }

}
