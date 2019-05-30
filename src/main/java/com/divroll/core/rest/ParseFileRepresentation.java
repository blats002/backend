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
package com.divroll.core.rest;

import com.divroll.core.rest.resource.GaeRootServerResource;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.io.CountingOutputStream;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Representation that buffers Parse files
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class ParseFileRepresentation extends OutputRepresentation {

    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    final static Logger LOG
            = LoggerFactory.getLogger(ParseFileRepresentation.class);

    private String fileUrl;

    private String parseBase;

    private String parseAppId;

    private String parseRestApiKey;

    public ParseFileRepresentation(String parseBase, String parseAppId, String parseRestApiKey, String fileUrl, MediaType mediaType) {
        super(mediaType);
        setFileUrl(fileUrl);
        setParseBase(parseBase);
        setParseAppId(parseAppId);
        setParseRestApiKey(parseRestApiKey);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
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
                updateAppUsage(getParseAppId(), countingOutputStream.getCount());
            }
        };
        Thread t = new Thread(runnable);
        t.run();
        outputStream.close();
    }

    public void getFile(String subdomain, String completePath, OutputStream outputStream) throws IOException {

    }

    public void setParseRestApiKey(String restApiKey) {
        this.parseRestApiKey = restApiKey;
    }

    public void setParseBase(String parseBase) {
        this.parseBase = parseBase;
    }

    public String getParseAppId() {
        return parseAppId;
    }

    public void setParseAppId(String parseAppId) {
        this.parseAppId = parseAppId;
    }

    private void updateAppUsage(String appId, Long newBytes) {
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

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
