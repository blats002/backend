/**
 *
 * Copyright (c) 2014 Kerby Martino and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.server.BlobFile;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.google.appengine.api.datastore.Blob;
import org.restlet.data.MediaType;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.resource.Get;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

import static com.hunchee.twist.ObjectStoreService.store;

/**
 * Resource which has only one representation.
 */
public class GaeRootServerResource extends SelfInjectingServerResource {

    private static final Logger LOG
            = Logger.getLogger(GaeFileServerResource.class.getName());

    @Get
    public ByteArrayRepresentation represent() {
        String path = getRequest().getResourceRef().getPath();
        if(path.startsWith("/")){
            path = path.substring(1);
        }
        LOG.info("Path: " + path);
        BlobFile blobFile = store().get(BlobFile.class, path);
        if(blobFile != null){
            return new ByteArrayRepresentation(blobFile.getBlob().getBytes(), processMediaType(path));
        }
//        ByteArrayRepresentation bar
//                = new ByteArrayRepresentation(your_images_bytes, MediaType.IMAGE_JPEG) ;
//        getResponse().setEntity(bar);
        return null;
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
        }
        return type;
    }
}