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

import com.divroll.backend.model.File;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.repository.CustomCodeRepository;
import com.divroll.backend.resource.CustomCodesResource;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteStreams;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class JeeCustomCodesServerResource extends BaseServerResource
    implements CustomCodesResource {

    private static final Logger LOG = LoggerFactory.getLogger(JeeCustomCodesServerResource.class);

    @Inject
    CustomCodeRepository customCodeRepository;

    @Override
    public Representation upload(Representation entity) {
        try {
            if (!isSuperUser() && !isMaster()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if ( (appId == null || appId.isEmpty()) ) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            Superuser superuser = superuserRepository.getUserByAuthToken(superAuthToken);
            if(superuser == null) {
                return unauthorized();
            }

            if (entity != null && MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                Request restletRequest = getRequest();
                HttpServletRequest servletRequest = ServletUtils.getRequest(restletRequest);
                ServletFileUpload upload = new ServletFileUpload();
                FileItemIterator fileIterator = upload.getItemIterator(servletRequest);
                File file = null;
                while (fileIterator.hasNext()) {
                    FileItemStream item = fileIterator.next();
                    if (item.isFormField()) {
                    } else {
                        byte[] bytes = ByteStreams.toByteArray(item.openStream());
                        String entityId = customCodeRepository.createCustomCode(appId, namespace,
                                customCodeName,
                                "",
                                1000L,
                                new ByteArrayInputStream(bytes));
                        setStatus(Status.SUCCESS_CREATED);
                        break;
                    }
                }
            } else if (entity != null
                    && MediaType.APPLICATION_OCTET_STREAM.equals(entity.getMediaType())) {
                InputStream inputStream = entity.getStream();
                byte[] bytes = ByteStreams.toByteArray(inputStream);
                String entityId = customCodeRepository.createCustomCode(appId, namespace,
                        customCodeName,
                        "",
                        1000L,
                        new ByteArrayInputStream(bytes));
                setStatus(Status.SUCCESS_CREATED);
            } else {
                badRequest();
            }
        } catch (Exception e) {
            return serverError();
        }
        return null;
    }

    @Override
    public Representation list() {

        // TODO - Add master key auth
        Superuser superuser = superuserRepository.getUserByAuthToken(superAuthToken);
        if(superuser == null) {
            return unauthorized();
        }

        List entityObjs = customCodeRepository.listCustomCodes(appId, namespace, superuser.getEntityId());
        JSONObject responseBody = new JSONObject();
        JSONObject customCodesJSONObject = new JSONObject();
        customCodesJSONObject.put("results", entityObjs);
        customCodesJSONObject.put("skip", 0);
        customCodesJSONObject.put("limit", 100);
        responseBody.put("entities", customCodesJSONObject);
        return success(responseBody);
    }

}
