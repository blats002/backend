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
package com.divroll.core.rest;

import com.divroll.core.rest.service.CacheService;
import com.divroll.core.rest.util.CachingOutputStream;
import com.divroll.core.rest.util.StringUtil;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteSource;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class DivrollFileRepresentation extends OutputRepresentation {

    final static Logger LOG = LoggerFactory.getLogger(DivrollFileRepresentation.class);

    private String path;
    private String appName;
    private String domain;
    private CacheService cacheService;
    private String baseUri;
    private String masterToken;

    public DivrollFileRepresentation(String masterToken, String appName, String domainName, String baseUri, String path, MediaType mediaType,
                                     CacheService cacheService) {
        super(mediaType);
        this.appName = appName;
        this.path = path;
        this.cacheService = cacheService;
        this.baseUri = baseUri;
        this.masterToken = masterToken;
        this.domain = domainName;
    }

    public DivrollFileRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            LOG.info("Writing bytes...");
            if(appName != null) {
                String cacheKey = appName + ":" + path;
                byte[] buff = new byte[128*1024];
                Date start = new Date();
                byte[] cached = cacheService.get(cacheKey);
                Date finished = new Date();
                LOG.info("TOTAL FETCHED CACHE: " + (finished.getTime() - start.getTime()));
                if(cached != null && cached.length > 0) {
                    start = new Date();
                    flow(ByteSource.wrap(cached).openStream(), outputStream, buff);
                    finished = new Date();
                    LOG.info("CACHED FLOW DONE at " + (finished.getTime() - start.getTime()) );
                } else {
                    HttpResponse<InputStream> response = Unirest.get(baseUri + "/files")
                            .header("X-Divroll-Master-Token", masterToken)
                            .queryString("appName", appName)
                            .queryString("filePath", path)
                            .asBinary();
                    if(response.getStatus() == 200) {
                        InputStream is = response.getBody();
                        final CachingOutputStream cachingOutputStream = new CachingOutputStream(outputStream);
                        start = new Date();
                        flow(is, cachingOutputStream, buff);
                        finished = new Date();
                        LOG.info("FLOW DONE at " + (finished.getTime() - start.getTime()) );
                        cached = cachingOutputStream.getCache();
                        if(cached != null && cached.length > 0) {
                            cacheService.put(cacheKey, cached);
                        }
                    }
                }

            } else if(domain != null) {
                String cacheKey = domain + ":" + path;
                byte[] buff = new byte[64*1024];
                byte[] cached = cacheService.get(cacheKey);
                if(cached != null && cached.length > 0) {
                    Date start = new Date();
                    flow(ByteSource.wrap(cached).openStream(), outputStream, buff);
                    Date finished = new Date();
                    LOG.info("CACHED FLOW DONE at " + (finished.getTime() - start.getTime()) );
                } else {
                    HttpResponse<InputStream> response = Unirest.get(baseUri + "/files")
                            .header("X-Divroll-Master-Token", masterToken)
                            .queryString("domainName", domain)
                            .queryString("filePath", path)
                            .asBinary();
                    if(response.getStatus() == 200) {
                        InputStream is = response.getBody();
                        final CachingOutputStream cachingOutputStream = new CachingOutputStream(outputStream);
                        flow(is, cachingOutputStream, buff);
                        cached = cachingOutputStream.getCache();
                        if(cached != null && cached.length > 0) {
                            cacheService.put(cacheKey, cached);
                        }
                    }
                }
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            outputStream.close();
        } finally {
            outputStream.close();
        }
    }

    private static void flow(InputStream is, OutputStream os, byte[] buf )
            throws IOException {
        int numRead;
        while ( (numRead = is.read(buf) ) >= 0) {
            os.write(buf, 0, numRead);
        }
    }

    protected String read404template(){
        InputStream is = this.getClass().getResourceAsStream("/error404.html");
        return StringUtil.toString(is);
    }


}
