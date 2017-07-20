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

import com.divroll.core.rest.service.CacheService;
import com.divroll.core.rest.util.CachingOutputStream;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class CloudFileRepresentation extends OutputRepresentation {

    final static Logger LOG = LoggerFactory.getLogger(CloudFileRepresentation.class);
    private static final String PROJECT_ID = "873831973341";
    private static final String BUCKET = "divrolls";

    private String path;
    private CacheService cacheService;

    public CloudFileRepresentation(String path, MediaType mediaType, CacheService cacheService) {
        super(mediaType);
        this.path = path;
        this.cacheService = cacheService;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            LOG.info(path);
            InputStream stream = new ByteArrayInputStream(GoogleJsonKey.JSON_KEY.getBytes(StandardCharsets.UTF_8));
            StorageOptions options = StorageOptions.newBuilder()
                    .setProjectId(PROJECT_ID)
                    .setCredentials(GoogleCredentials.fromStream(stream)).build();
            Storage storage = options.getService();

//            final CountingOutputStream countingOutputStream = new CountingOutputStream(outputStream);
//            byte[] read = storage.readAllBytes(BlobId.of(BUCKET, path));
//            countingOutputStream.write(read);

            BlobId blobId = BlobId.of(BUCKET, path);
            Blob blob = storage.get(blobId);
            ReadChannel reader = blob.reader();
            byte[] buff = new byte[64*1024];
            final CachingOutputStream cachingOutputStream = new CachingOutputStream(outputStream);
            InputStream is = Channels.newInputStream(reader);
            flow(is, cachingOutputStream, buff);
            byte[] cached = cachingOutputStream.getCache();
            cacheService.put(path, cached);
            cachingOutputStream.close();

        } catch (Exception e) {
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

}
