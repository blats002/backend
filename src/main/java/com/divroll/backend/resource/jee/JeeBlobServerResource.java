/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
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
package com.divroll.backend.resource.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.helper.ACLHelper;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.EntityStub;
import com.divroll.backend.model.Role;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.resource.BlobResource;
import com.divroll.backend.service.WebTokenService;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeBlobServerResource extends BaseServerResource
        implements BlobResource {

    @Inject
    EntityRepository entityRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public void setBlob(Representation entity) {
        try {
            if (!isAuthorized()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            if (entity == null || entity.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }

            String encoding = getQueryValue("encoding");

            String authUserId = null;

            boolean isWriteAccess = false;
            boolean isMaster = false;
            boolean isPublic = false;

            try {
                authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            } catch (Exception e) {
                // do nothing
            }

            Map<String, Object> map = entityRepository.getEntity(appId, entityType, entityId);
            List<EntityStub> aclWriteList = map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
                    ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE) : new LinkedList<>();

            if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
                isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
            }

            if (isMaster()) {
                isMaster = true;
            } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
                isWriteAccess = true;
            } else if (authUserId != null) {
                List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                for (Role role : roles) {
                    if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
                        isWriteAccess = true;
                    }
                }
            }

            if (isMaster || isWriteAccess || isPublic) {
                // TODO: Compress stream
                if(encoding != null && encoding.equals("base64")) {
                    String base64 = entity.getText();
                    byte[] bytes = BaseEncoding.base64().decode(base64);
                    InputStream inputStream = ByteSource.wrap(bytes).openStream();
                    if (entityRepository.createEntityBlob(appId, entityType, entityId, blobName, inputStream)) {
                        setStatus(Status.SUCCESS_CREATED);
                    } else {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                } else {
                    if (entityRepository.createEntityBlob(appId, entityType, entityId, blobName, entity.getStream())) {
                        setStatus(Status.SUCCESS_CREATED);
                    } else {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                }

            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }

        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public void updateBlob(Representation entity) {
        setBlob(entity);
    }

    @Override
    public void deleteBlob(Representation entity) {
        try {
            if (!isAuthorized()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            Application app = applicationService.read(appId);
            if (app == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }

            String authUserId = null;

            boolean isWriteAccess = false;
            boolean isMaster = false;
            boolean isPublic = false;

            try {
                authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            } catch (Exception e) {
                // do nothing
            }

            Map<String, Object> map = entityRepository.getEntity(appId, entityType, entityId);
            List<EntityStub> aclWriteList = map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
                    ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE) : new LinkedList<>();

            if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
                isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
            }

            if (isMaster()) {
                isMaster = true;
            } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
                isWriteAccess = true;
            } else if (authUserId != null) {
                List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                for (Role role : roles) {
                    if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
                        isWriteAccess = true;
                    }
                }
            }

            if (isMaster || isWriteAccess || isPublic) {
                if (entityRepository.deleteEntityBlob(appId, entityType, entityId, blobName)) {
                    setStatus(Status.SUCCESS_OK);
                } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }

        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public Representation getBlob(Representation entity) {
        try {
            if (!isAuthorized()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }

            String authUserId = null;

            boolean isWriteAccess = false;
            boolean isMaster = false;
            boolean isPublic = false;

            try {
                authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            } catch (Exception e) {
                // do nothing
            }

            Map<String, Object> map = entityRepository.getEntity(appId, entityType, entityId);
            List<EntityStub> aclWriteList = map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
                    ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE) : new LinkedList<>();

            if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
                isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
            }

            if (isMaster()) {
                isMaster = true;
            } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
                isWriteAccess = true;
            } else if (authUserId != null) {
                List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                for (Role role : roles) {
                    if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
                        isWriteAccess = true;
                    }
                }
            }

            String encoding = getQueryValue("encoding");

            if (isMaster || isWriteAccess || isPublic) {
                InputStream is = entityRepository.getEntityBlob(appId, entityType, entityId, blobName);

                if(encoding != null && encoding.equals("base64")) {
                    if (is == null) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return null;
                    } else {
                        String base64 = BaseEncoding.base64().encode(ByteStreams.toByteArray(is));
                        Representation representation = new StringRepresentation(base64);
                        setStatus(Status.SUCCESS_OK);
                        return representation;
                    }
                } else {
                    if (is == null) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return null;
                    } else {
                        Representation representation = new InputRepresentation(is);
                        setStatus(Status.SUCCESS_OK);
                        return representation;
                    }
                }


            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }

        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }
}