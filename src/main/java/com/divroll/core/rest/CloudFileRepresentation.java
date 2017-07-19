package com.divroll.core.rest;

import com.divroll.core.rest.util.CachingOutputStream;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.io.CountingOutputStream;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Kerby on 7/10/2017.
 */
public class CloudFileRepresentation extends OutputRepresentation {

    final static Logger LOG
            = LoggerFactory.getLogger(CloudFileRepresentation.class);
    private static final String PROJECT_ID = "873831973341";
    private static final String BUCKET = "divrolls";

    private String path;

    private MemcachedClient mc;
    private List<String> connections;
    private int memcachedTimout;
    private int memcahcedExpiry;

    public CloudFileRepresentation(String path, MediaType mediaType, MemcachedClient mc, int memcachedTimout, int memcachedExpiry, List<String> connections) {
        super(mediaType);
        this.path = path;
        this.mc = mc;
        this.connections = connections;
        this.memcachedTimout = memcachedTimout;
        this.memcahcedExpiry = memcachedExpiry;

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
            byteCachePut(path, memcahcedExpiry, cached);

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

    private void byteCachePut(String key, int expiration, byte[] value) {
        LOG.info("Byte caching: " + key);
        ConnectionFactory factory = null;
        try {
            if(mc == null) {
                factory = new ConnectionFactoryBuilder()
                        .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
                        .setOpTimeout(memcachedTimout)
                        .build();
                mc = new MemcachedClient(factory, AddrUtil.getAddresses(connections));
            }
            mc.set(key, expiration, value).get();
        } catch (Exception e) {
            e.printStackTrace();
            mc.shutdown();
            mc = null;
        } finally {
            //mc.shutdown();
        }
    }
}
