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
package com.divroll.roll.resource.jee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.divroll.roll.helper.ACLHelper;
import com.divroll.roll.model.Application;
import com.divroll.roll.model.exception.ACLException;
import com.divroll.roll.resource.KeyValueResource;
import com.divroll.roll.service.KeyValueService;
import com.divroll.roll.service.WebTokenService;
import com.divroll.roll.xodus.XodusEnvStore;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeKeyValueServerResource extends BaseServerResource
        implements KeyValueResource {

    @Inject
    KeyValueService keyValueService;

    @Inject
    WebTokenService webTokenService;

    @Inject
    XodusEnvStore store;

    @Override
    public Representation getValue() {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (authToken == null || authToken.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                return null;

            }

            String authUsername = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            String uuidKey = "username:" + authUsername + ":uuid";
            String authUUID = store.get(appId, authUsername, uuidKey, String.class);


            String dir = appId;
            if (dir != null) {
                if (accept != null) {
                    if (accept.equals(MediaType.APPLICATION_OCTET_STREAM) || accept.equals("application/octet-stream")) {
                        ByteBuffer value = keyValueService.get(appId, entityType, entityId, authUUID, ByteBuffer.class);
                        if (value != null) {
                            byte[] arr = new byte[value.remaining()];
                            value.get(arr);
                            Representation representation = new ByteArrayRepresentation(arr);
                            representation.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
                            setStatus(Status.SUCCESS_OK);
                            return representation;
                        }
                    } else if (accept.equals(MediaType.APPLICATION_JSON)) {
                        String value = keyValueService.get(appId, entityType, entityId, authUUID, String.class);
                        if (value != null) {
                            Representation representation = new JsonRepresentation(value);
                            setStatus(Status.SUCCESS_OK);
                            return representation;
                        } else {
                            return null;
                        }
                    } else {
                        // default
                        String value = keyValueService.get(appId, entityType, entityId, authUUID, String.class);
                        if (value != null) {
                            Representation representation = new StringRepresentation(value);
                            setStatus(Status.SUCCESS_OK);
                            return representation;
                        } else {
                            return null;
                        }
                    }
                } else {
                    // treat as String
                    String value = keyValueService.get(appId, entityType, entityId, authUUID, String.class);
                    boolean success = keyValueService.delete(appId, entityType, entityId, authUUID);
                    if (success) {
                        Representation representation = new StringRepresentation(value);
                        setStatus(Status.SUCCESS_OK);
                        return representation;
                    } else {
                        setStatus(Status.SERVER_ERROR_INTERNAL);
                    }
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public Representation createValue(Representation entity) {
        JSONObject result = new JSONObject();
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entity == null || entity.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }

            String[] read = new String[]{};
            String[] write = new String[]{};

            if (aclRead != null) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclRead);
                    read = ACLHelper.onlyIds(jsonArray);
                } catch (Exception e) {
                    // do nothing
                }
            }

            if (aclWrite != null) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclWrite);
                    List<String> aclWriteList = new LinkedList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        aclWriteList.add(jsonArray.getString(i));
                    }
                    write = aclWriteList.toArray(new String[aclWriteList.size()]);
                } catch (Exception e) {
                    // do nothing
                }
            }

            String dir = appId;
            if (dir != null) {
                if (contentType.equals(MediaType.APPLICATION_OCTET_STREAM) || contentType.equals("application/octet-stream")) {
                    byte[] value = ByteStreams.toByteArray(entity.getStream());
                    boolean success = keyValueService.putIfNotExists(appId, entityType, entityId, ByteBuffer.wrap(value),
                            read, write, ByteBuffer.class);
                    if (success) {
                        setStatus(Status.SUCCESS_CREATED);
                    } else {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                } else {
                    String value = entity.getText();
                    boolean success = keyValueService.putIfNotExists(appId, entityType, entityId, value,
                            read, write, String.class);
                    if (success) {
                        setStatus(Status.SUCCESS_CREATED);
                    } else {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                }
            }
        } catch (ACLException e) {
            e.printStackTrace();
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        Representation representation = new JsonRepresentation(result.toJSONString());
        return representation;
    }

    @Override
    public Representation updateValue(Representation entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entity == null || entity.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
            if (authToken == null || authToken.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return returnMissingAuthToken();
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                return returnNull();

            }

            String[] read = new String[]{};
            String[] write = new String[]{};

            if (aclRead != null) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclRead);
                    read = ACLHelper.onlyIds(jsonArray);
                } catch (Exception e) {
                    // do nothing
                }
            }

            if (aclWrite != null) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclWrite);
                    List<String> aclWriteList = new LinkedList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        aclWriteList.add(jsonArray.getString(i));
                    }
                    write = aclWriteList.toArray(new String[aclWriteList.size()]);
                } catch (Exception e) {
                    // do nothing
                }
            }

            String authUsername = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            String uuidKey = "username:" + authUsername + ":uuid";
            String authUUID = store.get(appId, authUsername, uuidKey, String.class);

            String dir = appId;
            if (dir != null) {
                if (contentType.equals(MediaType.APPLICATION_OCTET_STREAM) || contentType.equals("application/octet-stream")) {
                    byte[] value = ByteStreams.toByteArray(entity.getStream());
                    boolean success = keyValueService.put(appId, entityType, entityId, ByteBuffer.wrap(value), authUUID,
                            read, write, ByteBuffer.class);
                    if (success) {
                        setStatus(Status.SUCCESS_NO_CONTENT);
                        return null;
                    } else {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return null;
                    }
                } else {
                    String value = entity.getText();
                    try {
                        // Try to put the value, if it throws then this is a unauthorized request
                        boolean success = keyValueService.put(appId, entityType, entityId, value, authUUID,
                                read, write, String.class);
                        if (success) {
                            setStatus(Status.SUCCESS_NO_CONTENT);
                            return null;
                        } else {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                            return null;
                        }
                    } catch (ACLException e) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                    }
                }
            }

        } catch (ACLException e) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new StringRepresentation("");
    }

    @Override
    public void deleteValue(Representation entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            if (entity == null || entity.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
            if (authToken == null || authToken.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                return;

            }

            String authUsername = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            String uuidKey = "username:" + authUsername + ":uuid";
            String authUUID = store.get(appId, authUsername, uuidKey, String.class);


            String dir = appId;
            if (dir != null) {
                try {
                    // Try to put the value, if it throws then this is a unauthorized request
                    boolean success = keyValueService.delete(appId, entityType, entityId, authUUID);
                    if (success) {
                        setStatus(Status.SUCCESS_OK);
                    } else {
                        setStatus(Status.SERVER_ERROR_INTERNAL);
                    }
                } catch (ACLException e) {
                    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

}
