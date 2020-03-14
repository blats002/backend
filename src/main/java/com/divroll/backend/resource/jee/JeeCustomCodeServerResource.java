/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.resource.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.repository.CustomCodeRepository;
import com.divroll.backend.resource.CustomCodeResource;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JeeCustomCodeServerResource extends BaseServerResource
    implements CustomCodeResource {

    String customCodeName;

    @Inject
    CustomCodeRepository customCodeRepository;

    @Override
    protected void doInit() {
        super.doInit();
        customCodeName = getAttribute("customCodeName");
    }

    @Override
    public Representation getJar(Representation entity) {
        if(!isMaster()) {
            return unauthorized();
        }
        try {
            InputStream is = customCodeRepository.getCustomCode(appId, namespace, customCodeName);
            Representation representation = new InputRepresentation(is);
            representation.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
//            Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
//            representation.setDisposition(disposition);
            setStatus(Status.SUCCESS_OK);
            return representation;

        } catch (Exception e) {
            return internalError(stackTraceToString(e));
        }
    }

    @Override
    public Representation createJar(Representation entity) {
        if(!isMaster()) {
            return unauthorized();
        }
        try {
            InputStream is = null;
            // TODO - Check if stream is JAR file
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                try {
                    DiskFileItemFactory factory = new DiskFileItemFactory();
                    factory.setSizeThreshold(1000240);
                    RestletFileUpload upload = new RestletFileUpload(factory);
                    FileItemIterator fileIterator = upload.getItemIterator(entity);
                    while (fileIterator.hasNext()) {
                        FileItemStream fi = fileIterator.next();
                        byte[] bytes = ByteStreams.toByteArray(fi.openStream());
                        is = new ByteArrayInputStream(bytes);
                    }
                } catch (Exception e) {
                    internalError(stackTraceToString(e));
                }
            } else {
                is = entity.getStream();
            }
            String entityId = customCodeRepository.createCustomCode(appId, namespace,
                    customCodeName,
                    "",
                    1000L,
                    is);
            JSONObject entityObject = new JSONObject();
            entityObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
            JSONObject result = new JSONObject();
            result.put("entity", entityObject);
            return created(result);
        } catch (Exception e) {
            return internalError(stackTraceToString(e));
        }
    }

    @Override
    public Representation updateJar(Representation entity) {
        if(!isMaster()) {
            return unauthorized();
        }
        return badRequest();
    }

    @Override
    public Representation deleteJar(Representation entity) {
        if(!isMaster()) {
            return unauthorized();
        }
        try {
            if(customCodeRepository.deleteCustomCode(appId, namespace, customCodeName)) {
                return success();
            } else {
                return badRequest();
            }
        } catch (Exception e) {
            return internalError(stackTraceToString(e));
        }
    }

    @Override
    public void optionsMethod(Representation entity) {

    }
}
