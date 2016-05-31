package com.divroll.core.rest;

import com.alibaba.fastjson.JSON;
import com.divroll.core.rest.resource.gae.GaeRootServerResource;
import com.divroll.core.rest.util.CachingOutputStream;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.AppengineHttpRequestor;
import com.dropbox.core.v1.DbxClientV1;
import com.dropbox.core.v1.DbxEntry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class DropBoxFileRepresentation extends OutputRepresentation {

    private static final String KEY_SPACE = ":";
    private static final String ROOT_URI = "/";

    final static Logger LOG
            = LoggerFactory.getLogger(DropBoxFileRepresentation.class);


    private String path = null;

    private String dropboxToken;

    public DropBoxFileRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    public DropBoxFileRepresentation(String path, String token, MediaType mediaType) {
        super(mediaType);
        setPath(path);
        setDropboxToken(token);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        DbxRequestConfig config = new DbxRequestConfig("weebio/1.0", Locale.getDefault().toString(), AppengineHttpRequestor.Instance);
        DbxClientV1 client = new DbxClientV1(config, dropboxToken);
        DbxEntry.File md;
        String completePath = this.path;
        try {
            long numBytes = 0;
            Object cached = null;
            if(cached != null){
                outputStream.write((byte[]) cached);
                numBytes = ((byte[]) cached).length;
            } else {
                CachingOutputStream cache = null;
                if(completePath.endsWith(ROOT_URI)) {
                    LOG.debug("Files in the root path:");
                    DbxEntry.WithChildren listing = client.getMetadataWithChildren(
                            completePath.substring(0,completePath.length()-1));
                    Map directory = new HashMap<>();
                    List<String> list = new ArrayList<>();
                    for (DbxEntry child : listing.children) {
                        list.add(child.path);
                    }
                    directory.put("directory", list);
                    String jsonString = JSON.toJSONString(directory);
                    outputStream.write(jsonString.getBytes());
                } else {
//								OutputStream buff;
//								kinveyService.getFile(subdomain, pathParts, revision,
//										cache = new CachingOutputStream(buff = new CountingOutputStream(outputStream)));
//								numBytes = ((CountingOutputStream) buff).getCount();
//								LOG.info("File size: " + numBytes);
//								if(ByteHelper.bytesToMeg(numBytes) <= 1) {
//									LOG.info("Caching file: " + completePath);
//                                    System.out.println("Caching file: " + completePath);
//									memCache.put(key, cache.getCache());
//								}
//                    md = client.getFile(completePath, null,  cache = new CachingOutputStream(outputStream));
                    md = client.getFile(completePath, null,  outputStream);
                    if (md == null) {
                        LOG.debug("File metadata not found: " + completePath);
                    } else {
                        numBytes = md.numBytes;
//                    if(cache != null && (ByteHelper.bytesToMeg(numBytes) <= 1)){
//                        LOG.info("Caching file: " + completePath);
//                        memCache.put(key, cache.getCache(), Expiration.byDeltaMillis(EXPIRATION));
//                    }
                    }

//                    com.google.appengine.api.taskqueue.Queue queue = QueueFactory.getDefaultQueue();
//                    queue.add(TaskOptions.Builder
//                            .withUrl("/rest/metrics")
//                            .param("subdomain", subdomain)
//                            .param("numbytes", String.valueOf(numBytes)));

                }
            }
        } catch (DbxException e) {
            e.printStackTrace();
            String error = "Error serving that request. Please try again.";
            outputStream.write(error.getBytes());
        } catch (Exception e){
            e.printStackTrace();
            String error = "Error serving that request. Please try again.";
            outputStream.write(error.getBytes());
        }
        outputStream.close();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void getFile(String subdomain, String completePath, OutputStream outputStream) throws IOException {

    }

    public void setDropboxToken(String dropboxToken) {
        this.dropboxToken = dropboxToken;
    }
}
