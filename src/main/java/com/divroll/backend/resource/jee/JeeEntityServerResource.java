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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.divroll.backend.Constants;
import com.divroll.backend.helper.ACLHelper;
import com.divroll.backend.helper.JSON;
import com.divroll.backend.helper.ObjectLogger;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.EntityStub;
import com.divroll.backend.model.Role;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.EntityResource;
import com.divroll.backend.service.ApplicationService;
import com.divroll.backend.service.WebTokenService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
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
    RoleRepository roleRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ApplicationService applicationService;

    @Inject
    WebTokenService webTokenService;

    @Inject
    @Named("defaultUserStore")
    String storeName;

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
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("entity", entityObj);
                    setStatus(Status.SUCCESS_OK);
                    return new JsonRepresentation(jsonObject);
                } else {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            } else {

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
                    ObjectLogger.log(entityObj);
                    List<EntityStub> aclReadList = (List<EntityStub>) (entityObj.get("aclRead"));
                    publicRead = (Boolean) (entityObj).get(Constants.RESERVED_FIELD_PUBLICREAD);
                    if (authUserId != null && ACLHelper.contains(authUserId, aclReadList)) {
                        isAccess = true;
                    } else if(authUserId != null){
                        List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                        for (Role role : roles) {
                            if (ACLHelper.contains(role.getEntityId(), aclReadList)) {
                                isAccess = true;
                            }
                        }
                    }
                    if ((publicRead != null && publicRead) || isAccess) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("entity", entityObj);
                        setStatus(Status.SUCCESS_OK);
                        return new JsonRepresentation(jsonObject);
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
//                JSONObject jsonObject = JSONObject.parseObject(entity.getText());
//                Iterator<String> it = jsonObject.keySet().iterator();

                org.json.JSONObject jsonObject = new org.json.JSONObject(entity.getText());
                org.json.JSONObject entityJSONObject = jsonObject.getJSONObject("entity");

                Map<String, Comparable> comparableMap = JSON.toComparableMap(entityJSONObject);

                Application app = applicationService.read(appId);
                if (app == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Application does not exists");
                    return null;
                }

                String[] read = new String[]{};
                String[] write = new String[]{};

                if (aclRead != null) {
                    try {
                        JSONArray jsonArray = JSONArray.parseArray(aclRead);
                        if(!ACLHelper.validate(jsonArray)) {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_INVALID_ACL);
                            return null;
                        }
                        read = ACLHelper.onlyIds(jsonArray);
                    } catch (Exception e) {
                        // do nothing
                    }
                }

                if (aclWrite != null) {
                    try {
                        JSONArray jsonArray = JSONArray.parseArray(aclWrite);
                        if(!ACLHelper.validate(jsonArray)) {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_INVALID_ACL);
                            return null;
                        }
                        write = ACLHelper.onlyIds(jsonArray);
                    } catch (Exception e) {
                        // do nothing
                    }
                }

                boolean isMaster = isMaster(appId, masterKey);

                if (isMaster) {
                    if (!comparableMap.isEmpty()) {
                        boolean success = entityRepository.updateEntity(appId, entityType, entityId, comparableMap, read, write, publicRead, publicWrite);
                        if (success) {
                            setStatus(Status.SUCCESS_OK);
                        } else {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        }
                    } else {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                } else {
                    if (!isMaster) {
                        Map<String, Object> entityMap = entityRepository.getEntity(appId, entityType, entityId);
                        String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                        if (entityMap == null) {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        } else {
                            Boolean publicWrite = (Boolean) entityMap.get(Constants.RESERVED_FIELD_PUBLICWRITE);
                            Boolean authUserIdWriteAllow = false;

                            List<EntityStub> aclWriteList = new LinkedList<EntityStub>();
                            if (entityMap.get(Constants.ACL_WRITE) != null) {
                                aclWriteList = (List<EntityStub>) (entityMap.get(Constants.ACL_WRITE));
                            }
                            if (entityMap.get(Constants.ACL_WRITE) != null
                                    && ACLHelper.contains(authUserId, aclWriteList)) {
                                authUserIdWriteAllow = true;
                            }
                            if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
                                authUserIdWriteAllow = true;
                            } else if (authUserId != null) {
                                List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                                for (Role role : roles) {
                                    if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
                                        authUserIdWriteAllow = true;
                                    }
                                }
                            }

                            if ((publicWrite != null && publicWrite) || authUserIdWriteAllow) {
                                boolean success = entityRepository.updateEntity(appId, entityType, entityId, comparableMap, read, write, publicRead, publicWrite);
                                if (success) {
                                    setStatus(Status.SUCCESS_OK);
                                } else {
                                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                                }
                            } else {
                                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                            }
                        }
                    }
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
            if (entityId == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            Application app = applicationService.read(appId);
            if (app == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }
            if (!isMaster(appId, masterKey)) {
                Map<String, Object> entityMap = entityRepository.getEntity(appId, entityType, entityId);
                String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                if (entityMap == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                } else {

                    Boolean isAccess = false;
                    Boolean publicWrite = (Boolean) entityMap.get(Constants.RESERVED_FIELD_PUBLICWRITE);
                    Boolean authUserIdWriteAllow = false;

                    List<EntityStub> aclWriteList = new LinkedList<EntityStub>();
                    if (entityMap.get(Constants.ACL_WRITE) != null) {
                        aclWriteList = (List<EntityStub>) (entityMap.get(Constants.ACL_WRITE));
                    }

                    if (entityMap.get(Constants.ACL_WRITE) != null
                            && ACLHelper.contains(authUserId, aclWriteList)) {
                        authUserIdWriteAllow = true;
                    }

                    if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
                        isAccess = true;
                    } else if (authUserId != null) {
                        List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                        for (Role role : roles) {
                            if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
                                isAccess = true;
                            }
                        }
                    }

                    if (publicWrite == null) {
                        publicWrite = false;
                    }

                    if (publicWrite || authUserIdWriteAllow || isAccess) {
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
