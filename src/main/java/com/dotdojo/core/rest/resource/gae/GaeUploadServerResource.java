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

import com.divroll.core.rest.resource.UploadResource;
import com.divroll.core.rest.util.GAEUtil;
import com.divroll.core.rest.BlobFile;
import com.divroll.core.rest.guice.SelfInjectingServerResource;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.repackaged.com.google.common.io.ByteStreams;
import com.google.appengine.repackaged.org.apache.commons.fileupload.FileItemIterator;
import com.google.appengine.repackaged.org.apache.commons.fileupload.FileItemStream;
import com.google.appengine.repackaged.org.apache.commons.fileupload.servlet.ServletFileUpload;
import com.google.appengine.repackaged.org.apache.commons.fileupload.servlet.ServletRequestContext;
import com.google.apphosting.api.ApiProxy;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.hunchee.twist.ObjectStoreService.store;

public class GaeUploadServerResource extends SelfInjectingServerResource
    implements UploadResource {
    @Override
    public String upload(Representation entity) throws Exception {
        if (entity != null) {
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                Request restletRequest = getRequest();
                HttpServletRequest servletRequest = ServletUtils.getRequest(restletRequest);
                ServletFileUpload upload = new ServletFileUpload();
                FileItemIterator fileIterator = upload.getItemIterator(servletRequest);
                getLogger().info("content type: " + servletRequest.getContentType());
                getLogger().info("content: " + new ServletRequestContext(servletRequest).getContentLength());
                getLogger().info("iterator: " + fileIterator.hasNext());
                while (fileIterator.hasNext()) {
                    FileItemStream item = fileIterator.next();
                    String name = item.getName();
                    byte[] byteContent = ByteStreams.toByteArray(item.openStream());
                    // TODO byteContent is basically the file uploaded
                    getLogger().info("contentName: " + name);
                    getLogger().info("contentType: " + item.getContentType());
                    ObjectNode result = JsonNodeFactory.instance.objectNode();
                    processFile(byteContent, "dummy", getQueryValue("upload_type"), result);
                    return result.toString();
                }
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        return null;
    }

    private void processFile(byte[] bytes, String clientToken, String uploadType, ObjectNode result) {
        getLogger().info("token: " + clientToken);
        if("assets_zip".equals(uploadType)){
            try {
                ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(bytes));
                InputStreamReader isr = new InputStreamReader(zipStream);
                ZipEntry ze;
                while ((ze = zipStream.getNextEntry()) != null) {
                    String fileName = ze.getName();
                    getLogger().info("fileName: " + fileName);
                    long fileSize = ze.getCompressedSize();
                    ByteArrayOutputStream streamBuilder = new ByteArrayOutputStream();
                    int bytesRead;
                    byte[] tempBuffer = new byte[8192*2];
                    while ( (bytesRead = zipStream.read(tempBuffer)) != -1 ){
                        streamBuilder.write(tempBuffer, 0, bytesRead);
                    }
                    Blob blob = new Blob(streamBuilder.toByteArray());
                    BlobFile blobFile = new BlobFile(fileName, blob);
                    store().put(blobFile);
                }
                zipStream.close();
            } catch (IOException e){
                e.printStackTrace();
                result.put("success", "false");
            } catch (ApiProxy.RequestTooLargeException e){
                e.printStackTrace();
                result.put("success", "false");
                result.put("reason", "File too large");
            }

        } else if("blog_image".equals(uploadType)) {
//            ImagesService imageService = ImagesServiceFactory.getImagesService();
//            ServingUrlOptions serve = ServingUrlOptions.Builder.withBlobKey(blobKey);
//            String imageUrl = imageService.getServingUrl(serve);
//            User user = userService.read(webTokenService.readUserIdFromToken(clientToken));
//            user.getProfile().setPhoto(imageUrl);
//            userService.update(user);
//            result.put("image_url", imageUrl);
//            result.put("success", true);
//            getLogger().info("url: " + imageUrl);
        }
    }

    public byte[] getFile(String fileName){
        // TODO: Get file and put bytes here
        return null;
    }

    private String getBucketName(){
        if(GAEUtil.isGaeProd()){
            return "dotdojo.appspot.com";
        } else {
            return "localhost:8080";
        }
    }
}
