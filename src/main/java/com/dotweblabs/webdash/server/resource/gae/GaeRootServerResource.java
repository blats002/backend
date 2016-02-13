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
import com.divroll.webdash.server.util.GAEUtil;
import com.divroll.webdash.server.util.RegexHelper;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.AppengineHttpRequestor;
import com.dropbox.core.v1.DbxClientV1;
import com.dropbox.core.v1.DbxEntry;
import com.dropbox.core.v2.DbxClientV2;
import com.google.appengine.api.datastore.Blob;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static com.hunchee.twist.ObjectStoreService.store;

/**
 * Resource which has only one representation.
 */
public class GaeRootServerResource extends SelfInjectingServerResource {

    private static final Logger LOG
            = Logger.getLogger(GaeFileServerResource.class.getName());

    private static final String ROOT_URI = "/";
    private static final String APP_ROOT_URI = "/weebio/";

    @Inject
    @Named("app.domain")
    private String appDomain;

    @Inject
    @Named("app.domain.local")
    private String appDomainLocal;

    @Inject
    @Named("dropbox.token")
    private String dropboxToken;

    @Get
    public Representation represent() {
        Representation entity = null;
        MediaType type = getRequest().getEntity().getMediaType();
        String path = getRequest().getResourceRef().getPath();
        String _completePath = getRequest().getResourceRef().getHostIdentifier() +
                getRequest().getResourceRef().getPath();
        URL url = null;
        try {
            url = new URL(_completePath);
            String host = url.getHost();
            String subdomain = parseSubdomain(host);

            String pathParts = url.getPath();
            if(pathParts.isEmpty() || pathParts.equals(ROOT_URI)){
                pathParts = "/index.html";
            }
            final String completePath = APP_ROOT_URI + host + pathParts;

            LOG.info("Complete Path: " + completePath);
            LOG.info("Host: " + host);
            LOG.info("Subdomain: " + subdomain);

            entity = new OutputRepresentation(type) {
                @Override
                public void write(OutputStream outputStream) throws IOException {
                    DbxRequestConfig config = new DbxRequestConfig("weebio/1.0", Locale.getDefault().toString(), AppengineHttpRequestor.Instance);
                    DbxClientV1 client = new DbxClientV1(config, dropboxToken);
                    DbxEntry.File md;
                    try {
                        md = client.getFile(completePath, null,  outputStream);
                        System.out.println("File: " + completePath + " Bytes read: " + md.numBytes);
                    } catch (DbxException e) {
                        e.printStackTrace();
                    }
                    outputStream.close();
                }
            };
            entity.setMediaType(processMediaType(completePath));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
//        if(path.startsWith("/")){
//            path = path.substring(1);
//        }
//        if(path.equals("/") || path.isEmpty()) {
//            path = "index.html"; // TODO: Must be set through the dashboard e.g. index.htm or main.html etc.
//        }
//        LOG.info("Content-Type: " + type);
//        LOG.info("Path: " + path);
//        BlobFile blobFile = store().get(BlobFile.class, path);
//        if(blobFile != null){
//            return new ByteArrayRepresentation(blobFile.getBlob().getBytes(), processMediaType(path));
//        }
        return entity;
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

    private String parseSubdomain(String host){
        String domain;
        if(GAEUtil.isGaeDev()){
            domain = appDomainLocal;
        } else {
            domain = appDomain;
        }
        LOG.info("Parsing Host: " + host);
        LOG.info("Parsing Domain: " + domain);
        return RegexHelper.parseSubdomain(host, domain);
    }

}