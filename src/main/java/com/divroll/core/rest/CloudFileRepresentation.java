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
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

public class CloudFileRepresentation extends OutputRepresentation {

    final static Logger LOG = LoggerFactory.getLogger(CloudFileRepresentation.class);
    private static final String PROJECT_ID = "sonorous-cacao-192200";
    private static final String BUCKET = "appsbucket";

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
            if(blob != null && blob.exists()) {
                ReadChannel reader = blob.reader();
                byte[] buff = new byte[64*1024];
//                final CachingOutputStream cachingOutputStream = new CachingOutputStream(outputStream);
                InputStream is = Channels.newInputStream(reader);
//                flow(is, cachingOutputStream, buff);
//                byte[] cached = cachingOutputStream.getCache();
//                cacheService.put(path, cached);
//                cachingOutputStream.close();
                flow(is, outputStream, buff);
                //outputStream.close();
            } else {
                throw new FileNotFoundException(path);
            }
        } catch (FileNotFoundException e) {
            if(path.endsWith("index.html") || path.endsWith("index.htm")) {
                byte[] array = StringUtil.toByteArray(read404template());
                outputStream.write(array);
                outputStream.close();
            } else {
                outputStream.close();
                throw new FileNotFoundException(e.getMessage());
            }
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

    protected String read404template(){
        InputStream is = this.getClass().getResourceAsStream("/error404.html");
        return StringUtil.toString(is);
    }

}
