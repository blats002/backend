package com.divroll.core.rest;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.divroll.core.rest.service.CacheService;
import com.divroll.core.rest.util.CachingOutputStream;
import com.divroll.core.rest.util.StringUtil;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Executable;
import java.nio.charset.StandardCharsets;

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
