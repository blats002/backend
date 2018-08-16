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
package com.divroll.domino.resource.jee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.divroll.domino.Constants;
import com.divroll.domino.model.Application;
import com.divroll.domino.repository.EntityRepository;
import com.divroll.domino.resource.EntityResource;
import com.divroll.domino.service.ApplicationService;
import com.divroll.domino.service.WebTokenService;
import com.google.inject.Inject;
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.util.*;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeEntityServerResource extends BaseServerResource
        implements EntityResource {

    @Inject
    EntityRepository entityRepository;

    @Inject
    ApplicationService applicationService;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Representation getEntity() {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entityId == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing entity ID in request");
                return null;
            }
            Application app = applicationService.read(appId);
            if (app == null) {
                return null;
            }
            if (isMaster(appId, masterKey)) {
                Map<String, Object> entityObj = entityRepository.getEntity(appId, entityType, entityId);
                if (entityObj != null) {
                    setStatus(Status.SUCCESS_OK);
                    return new JsonRepresentation(entityObj);
                } else {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            } else {
//                if(authToken == null) {
//                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Nissing Auth Token in request");
//                    return null;
//                }
                String authUserId = null;

                try {
                    authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                } catch (Exception e) {
                    // do nothing
                }

                Boolean publicRead = false;
                Boolean isAccess = false;

                Map<String, Object> entityObj = entityRepository.getEntity(appId, entityType, entityId);
                if (entityObj != null) {
                    List<String> aclReadList = (List<String>) ((Map<String, Object>) entityObj.get("_md")).get("aclRead");
                    if (aclReadList.contains(Constants.ACL_ASTERISK)) {
                        publicRead = true;
                    } else if (authUserId != null && aclReadList.contains(authUserId)) {
                        isAccess = true;
                    }
                    if (publicRead || isAccess) {
                        setStatus(Status.SUCCESS_OK);
                        return new JsonRepresentation(cleanup(entityObj));
                    } else {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                    }
                } else {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }

            }

        } catch (EntityRemovedInDatabaseException e) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Entity was removed ");
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public Representation updateEntity(Representation entity) {
        JSONObject result = new JSONObject();
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entity == null || entity.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
            String dir = appId;
            if (dir != null) {
                JSONObject jsonObject = JSONObject.parseObject(entity.getText());
                Iterator<String> it = jsonObject.keySet().iterator();
                Map<String, Comparable> comparableMap = new LinkedHashMap<>();
                while (it.hasNext()) {
                    String k = it.next();
                    try {
                        JSONObject jso = jsonObject.getJSONObject(k);
                        // TODO
                        continue;
                    } catch (Exception e) {

                    }
                    try {
                        JSONArray jsa = jsonObject.getJSONArray(k);
                        // TODO
                        continue;
                    } catch (Exception e) {

                    }
                    try {
                        Boolean value = jsonObject.getBoolean(k);
                        comparableMap.put(k, value);
                        continue;
                    } catch (Exception e) {

                    }
                    try {
                        Long value = jsonObject.getLong(k);
                        comparableMap.put(k, value);
                        continue;
                    } catch (Exception e) {

                    }
                    try {
                        Double value = jsonObject.getDouble(k);
                        comparableMap.put(k, value);
                        continue;
                    } catch (Exception e) {

                    }
                    try {
                        String value = jsonObject.getString(k);
                        comparableMap.put(k, value);
                        continue;
                    } catch (Exception e) {

                    }
                }

                String[] read = new String[]{Constants.ACL_ASTERISK};
                String[] write = new String[]{Constants.ACL_ASTERISK};

                if (aclRead != null) {
                    try {
                        JSONArray jsonArray = JSONArray.parseArray(aclRead);
                        List<String> aclReadList = new LinkedList<>();
                        for (int i = 0; i < jsonArray.size(); i++) {
                            aclReadList.add(jsonArray.getString(i));
                        }
                        read = aclReadList.toArray(new String[aclReadList.size()]);
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
                if (!comparableMap.isEmpty()) {
                    boolean success = entityRepository.updateEntity(appId, entityType, entityId, comparableMap, read, write);
                    if (success) {
                        setStatus(Status.SUCCESS_OK);
                    } else {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        Representation representation = new JsonRepresentation(result.toJSONString());
        return representation;
    }

    @Override
    public void deleteEntity(Representation entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            if (roleId == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            Application app = applicationService.read(appId);
            if (app == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }
            if (!isMaster(appId, masterKey)) {
                Map<String, Object> entityMap = entityRepository.getEntity(appId, entityType, roleId);
                String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                if (entityMap == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                } else {
                    Boolean publicWrite = (Boolean) entityMap.get("publicWrite");
                    if (publicWrite || ((List<String>) entityMap.get("aclWrite")).contains(authUserId)) {
                        Boolean success = entityRepository.deleteEntity(appId, entityType, entityId);
                        if (success) {
                            setStatus(Status.SUCCESS_OK);
                        } else {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        }
                    } else {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                    }
                }
            } else {
                // Master key bypasses all checks
                Boolean success = entityRepository.deleteEntity(appId, entityType, entityId);
                if (success) {
                    setStatus(Status.SUCCESS_OK);
                } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return;
    }

}
