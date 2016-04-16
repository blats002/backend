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

import com.divroll.core.rest.Subdomain;
import com.divroll.core.rest.util.CachingOutputStream;
import com.divroll.core.rest.util.GAEUtil;
import com.divroll.core.rest.util.RegexHelper;
import com.divroll.core.rest.guice.SelfInjectingServerResource;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.AppengineHttpRequestor;
import com.dropbox.core.v1.DbxClientV1;
import com.dropbox.core.v1.DbxEntry;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kinvey.java.Query;
import com.kinvey.nativejava.AppData;
import com.kinvey.nativejava.Client;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Resource which has only one representation.
 */
public class GaeRootServerResource extends SelfInjectingServerResource {

    final static Logger LOG
            = LoggerFactory.getLogger(GaeRootServerResource.class);

    MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();

    private static final String ROOT_URI = "/";
    private static final String APP_ROOT_URI = "/weebio/";
	private static final String KEY_SPACE = ":";

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
        memCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    }

    @Get
    public Representation represent() {
        Representation entity = null;
        MediaType type = getRequest().getEntity().getMediaType();
        String path = getRequest().getResourceRef().getPath();
        String _completePath = getRequest().getResourceRef().getHostIdentifier() +
                getRequest().getResourceRef().getPath();
        URL url = null;
        try {
            url = new URL(_completePath);
            String host = url.getHost();

            String pathParts = url.getPath();
            if(pathParts.isEmpty() || pathParts.equals(ROOT_URI)){
                pathParts = "/index.html";
            }

            final String subdomain;
            if(!host.endsWith(getDomain())){
                subdomain = getStoredSubdomain(host);
            } else {
                subdomain = parseSubdomain(host);
            }

            pathParts = pathParts.replace("%20", " ");
            final String completePath = APP_ROOT_URI + subdomain + pathParts;

            LOG.info("Complete Path: " + completePath);
            LOG.info("Host: " + host);
            LOG.info("Subdomain: " + subdomain);

            entity = new OutputRepresentation(type) {
                @Override
                public void write(OutputStream outputStream) throws IOException {
                    DbxRequestConfig config = new DbxRequestConfig("weebio/1.0", Locale.getDefault().toString(), AppengineHttpRequestor.Instance);
                    DbxClientV1 client = new DbxClientV1(config, dropboxToken);
                    DbxEntry.File md;
                    try {
						long numBytes;
						String key = new StringBuilder()
								.append(subdomain)
								.append(KEY_SPACE)
								.append(completePath).toString();
						Object cached = memCache.get(key);
						if(cached != null){
							outputStream.write((byte[]) cached);
							numBytes = ((byte[]) cached).length;
						} else {
							CachingOutputStream cache = null;
							md = client.getFile(completePath, null,  cache = new CachingOutputStream(outputStream));
							numBytes = md.numBytes;
							if(cache != null && ((numBytes / 1024)/1024) <= 1){
								LOG.info("Caching file: " + completePath);
								memCache.put(key, cache.getCache());
							}
						}
						LOG.info("File: " + completePath + " Bytes read: " + numBytes);
                    } catch (DbxException e) {
                        e.printStackTrace();
                    }
                    outputStream.close();
                }
            };
            entity.setMediaType(processMediaType(completePath));

        } catch (MalformedURLException e) {
            e.printStackTrace();
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

    private String getDomain(){
        String domain;
        if(GAEUtil.isGaeDev()){
            domain = appDomainLocal;
        } else {
            domain = appDomain;
        }
        return domain;
    }

    private String getStoredSubdomain(String host){
        String result = null;
        try{
            String value = (String) memCache.get(host);
            if(value == null){
                Client kinvey = new Client.Builder(appkey, masterSecret).build();
                kinvey.enableDebugLogging();
                Boolean ping = kinvey.ping();
                LOG.info("Client ping -> " + ping);
                kinvey.user().loginBlocking(appkey, masterSecret).execute();
                LOG.info("Client login -> " + kinvey.user().isUserLoggedIn());
                String token = kinvey.user().getAuthToken();
                String userId = kinvey.user().getId();

                Query q = kinvey.query();
                q.equals("domain", host);

                AppData<Subdomain> subdomains = kinvey.appData("subdomains", Subdomain.class);
                Subdomain[] list = subdomains.getBlocking(q).execute();
                Subdomain subdomain = Arrays.asList(list).iterator().next();
                if(subdomain != null){
                    result = subdomain.getSubdomain();
                    memCache.put(host, subdomain.getSubdomain());
                    LOG.info("Subdomain for " + host + ": " + result);
                }
            } else {
                result = value;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }


    private String parseSubdomain(String host){
        String domain;
        if(GAEUtil.isGaeDev()){
            domain = appDomainLocal;
        } else {
            domain = appDomain;
        }
        LOG.info("Parsing Host: " + host);
        LOG.info("Parsing Domain: " + domain);
        return RegexHelper.parseSubdomain(host, domain);
    }

}
