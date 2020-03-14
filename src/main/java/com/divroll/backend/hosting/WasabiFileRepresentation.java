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
package com.divroll.backend.hosting;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.divroll.backend.service.CacheService;
import com.divroll.backend.util.StringUtil;
import com.divroll.backend.util.CachingOutputStream;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;

import java.io.*;

public class WasabiFileRepresentation extends OutputRepresentation {

    final static Logger LOG = LoggerFactory.getLogger(WasabiFileRepresentation.class);
    private static final String BUCKET = "divroll";

    private String path;
    private CacheService cacheService;

    public WasabiFileRepresentation(String path, MediaType mediaType, CacheService cacheService) {
        super(mediaType);
        this.path = path;
        this.cacheService = cacheService;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        try {
            LOG.info(path);
            BasicAWSCredentials credentials = new BasicAWSCredentials(WasabiCredential.ACCESS_KEY, WasabiCredential.SECRET_KEY);
            final AmazonS3 s3 = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("s3.wasabisys.com", "us-east-1"))
                    .build();
            S3Object o = s3.getObject(BUCKET, path);
            LOG.info("Content Length = " + o.getObjectMetadata().getContentLength());
            LOG.info("Content Type = " + o.getObjectMetadata().getContentType());
            InputStream is = o.getObjectContent();
            byte[] buff = new byte[64*1024];

            final CachingOutputStream cachingOutputStream = new CachingOutputStream(outputStream);
            flow(is, cachingOutputStream, buff);

            try {
                byte[] cached = cachingOutputStream.getCache();
                cacheService.put(path, cached);
            } catch (Exception e) {
                e.printStackTrace();
                cachingOutputStream.close();
            }

            //cachingOutputStream.close();
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
            if(e instanceof AmazonS3Exception) {
                AmazonS3Exception ax = (AmazonS3Exception) e;
                if(ax.getStatusCode() == 404) {
                    if(path.endsWith("/")) {

                    } else {

                    }
                }
            } else {
                e.printStackTrace();
                outputStream.close();
            }
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
