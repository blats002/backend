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
import com.divroll.backend.helper.*;
import com.divroll.backend.model.*;
import com.divroll.backend.model.builder.EntityClassBuilder;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.EntitiesResource;
import com.divroll.backend.service.PubSubService;
import com.divroll.backend.service.WebTokenService;
import com.divroll.backend.trigger.TriggerResponse;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import scala.actors.threadpool.Arrays;

import java.util.*;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeEntitiesServerResource extends BaseServerResource
        implements EntitiesResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeEntitiesServerResource.class);
    private static final Integer DEFAULT_LIMIT = 100;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    EntityRepository entityRepository;

    @Inject
    WebTokenService webTokenService;

    @Inject
    PubSubService pubSubService;

    @Inject
    @Named("defaultFunctionStore")
    String defaultFunctionStore;

    @Inject
    @Named("defaultUserStore")
    String defaultUserStore;

    @Inject
    @Named("defaultRoleStore")
    String defaultRoleStore;


    @Override
    public Representation createEntity(Representation entity) {
        JSONObject result = new JSONObject();
        try {
            if (!isAuthorized()) {
                return unauthorized();
            }
            if (entity == null || entity.isEmpty()) {
                return badRequest();
            }
            String dir = appId;
            if (dir != null) {

                JSONObject jsonObject = new JSONObject(entity.getText());
                JSONObject entityJSONObject = jsonObject.getJSONObject("entity");

                if (entityJSONObject == null) {
                    return badRequest();
                }

                Map<String, Comparable> comparableMap = JSON.jsonToMap(entityJSONObject);

                String[] read = new String[]{};
                String[] write = new String[]{};

                if (aclRead != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(aclRead);
                        read = ACLHelper.onlyIds(jsonArray);
                    } catch (Exception e) {
                        // do nothing
                    }
                }

                if (aclWrite != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(aclWrite);
                        write = ACLHelper.onlyIds(jsonArray);
                    } catch (Exception e) {
                        // do nothing
                    }
                }

                ObjectLogger.log(comparableMap);
                ObjectLogger.log(jsonObject);
                if (!comparableMap.isEmpty()) {

                    if (comparableMap.get("publicRead") != null) {
                        Comparable publicReadComparable = comparableMap.get("publicRead");
                        if (publicReadComparable instanceof Boolean) {
                            publicRead = (Boolean) publicReadComparable;
                        } else if (publicReadComparable instanceof String) {
                            publicRead = Boolean.valueOf((String) publicReadComparable);
                        }
                    }


                    if (comparableMap.get("publicWrite") != null) {
                        Comparable publicWriteComparable = comparableMap.get("publicWrite");
                        if (publicWriteComparable instanceof Boolean) {
                            publicWrite = (Boolean) publicWriteComparable;
                        } else if (publicWriteComparable instanceof String) {
                            publicWrite = Boolean.valueOf((String) publicWriteComparable);
                        }
                    }

                    //System.out.println("READ: " + ((EmbeddedArrayIterable) comparableMap.get(Constants.RESERVED_FIELD_ACL_READ)).asJSONArray());
                    //System.out.println("WRITE: " + ((EmbeddedArrayIterable) comparableMap.get(Constants.RESERVED_FIELD_ACL_WRITE)).asJSONArray());

                    if(comparableMap.get(Constants.RESERVED_FIELD_ACL_READ) != null) {
                        EmbeddedArrayIterable iterable = (EmbeddedArrayIterable) comparableMap.get(Constants.RESERVED_FIELD_ACL_READ);
                        JSONArray jsonArray = EntityIterables.toJSONArray(iterable);
                        read = ACLHelper.onlyIds(jsonArray);
                    }

                    if(comparableMap.get(Constants.RESERVED_FIELD_ACL_WRITE) != null) {
                        EmbeddedArrayIterable iterable = (EmbeddedArrayIterable) comparableMap.get(Constants.RESERVED_FIELD_ACL_WRITE);
                        JSONArray jsonArray = EntityIterables.toJSONArray(iterable);
                        write = ACLHelper.onlyIds(jsonArray);
                    }

                    if(read == null) {
                        read = new String[]{};
                    }
                    if(write == null) {
                        write = new String[]{};
                    }
                    if(publicRead == null) {
                        publicRead = true;
                    }
                    if(publicWrite == null) {
                        publicWrite = false;
                    }

                    validateSchema(entityType, comparableMap);

                    if(entityType.equalsIgnoreCase(defaultUserStore)) {
                        if (beforeSave(comparableMap, appId, entityType)) {

                            String username = (String) comparableMap.get(Constants.RESERVED_FIELD_USERNAME);
                            String password = (String) comparableMap.get(Constants.RESERVED_FIELD_PASSWORD);

                            if(username == null || password == null || username.isEmpty() || password.isEmpty()) {
                                return badRequest();
                            }

                            JSONArray roleJSONArray = comparableMap.get("roles") != null ? (JSONArray) comparableMap.get("roles") : null;
                            List<String> roleList = new LinkedList<>();
                            try {
                                for(int i = 0; i<roleJSONArray.length();i++) {
                                    JSONObject jsonValue = roleJSONArray.getJSONObject(i);
                                    if(jsonValue != null) {
                                        String roleId = jsonValue.getString(Constants.ROLE_ID);
                                        roleList.add(roleId);
                                    } else {
                                        String roleId = roleJSONArray.getString(i);
                                        roleList.add(roleId);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String entityId = userRepository.createUser(appId, entityType, username, password,
                                    comparableMap, read, write, publicRead, publicWrite, Iterables.toArray(roleList, String.class), actions);
                            JSONObject entityObject = new JSONObject();
                            entityObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
                            result.put("entity", entityObject);
                            comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
                            pubSubService.created(appId, entityType, entityId);
                            afterSave(comparableMap, appId, entityType);
                        }
                        afterSave(comparableMap, appId, entityType);
                    } else if(entityType.equalsIgnoreCase(defaultRoleStore)) {
                        if (beforeSave(comparableMap, appId, entityType)) {
                            String roleName = (String) comparableMap.get(Constants.ROLE_NAME);
                            roleRepository.createRole(appId, entityType, roleName, read, write, publicRead, publicWrite, actions);
                            afterSave(comparableMap, appId, entityType);
                        }
                    } else if(entityType.equalsIgnoreCase(defaultFunctionStore)) {
                        if (beforeSave(comparableMap, appId, entityType)) {
                            String entityId = entityRepository.createEntity(appId, entityType,
                                    new EntityClassBuilder()
                                            .comparableMap(comparableMap)
                                            .read(read)
                                            .write(write)
                                            .publicRead(publicRead)
                                            .publicWrite(publicWrite)
                                            .build(), actions, Arrays.asList(new String[]{Constants.RESERVED_FIELD_FUNCTION_NAME}));
                            JSONObject entityObject = new JSONObject();
                            entityObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
                            result.put("entity", entityObject);
                            comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
                            pubSubService.created(appId, entityType, entityId);
                            afterSave(comparableMap, appId, entityType);
                            return created(result);
                        } else {
                            return badRequest();
                        }
                    } else {
                        if(beforeSave(comparableMap, appId, entityType))  {
                            String entityId = entityRepository.createEntity(appId, entityType,
                                    new EntityClassBuilder()
                                            .comparableMap(comparableMap)
                                            .read(read)
                                            .write(write)
                                            .publicRead(publicRead)
                                            .publicWrite(publicWrite)
                                            .build(), actions, null);
                            JSONObject entityObject = new JSONObject();
                            entityObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
                            result.put("entity", entityObject);
                            comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
                            pubSubService.created(appId, entityType, entityId);
                            afterSave(comparableMap, appId, entityType);
                            return created(result);
                        } else {
                            return badRequest();
                        }
                    }
                } else {
                    return badRequest();
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return badRequest();
        } catch (Exception e) {
            e.printStackTrace();
            return serverError();
        }
        return null;
    }

    @Override
    public Representation getEntities() {
        try {
            if (!isAuthorized()) {
                return unauthorized();
            }

            int skipValue = 0;
            int limitValue = DEFAULT_LIMIT;

            if (skip != null && limit != null) {
                skipValue = skip;
                limitValue = limit;
            }

            if (isMaster()) {
                try {
                    List<Map<String, Comparable>> entityObjs
                            = entityRepository.listEntities(appId, entityType, null,
                            skipValue, limitValue, sort, true, filters);
                    JSONObject responseBody = new JSONObject();
                    JSONObject entitiesJSONObject = new JSONObject();
                    entitiesJSONObject.put("results", entityObjs);
                    entitiesJSONObject.put("skip", skipValue);
                    entitiesJSONObject.put("limit", limitValue);
                    responseBody.put("entities", entitiesJSONObject);
                    return success(responseBody);
                } catch (Exception e) {
                    return serverError();
                }
            } else {

                String authUserId = null;

                try {
                    authUserId = webTokenService.readUserIdFromToken(getApp().getMasterKey(), authToken);
                } catch (Exception e) {
                    // do nothing
                }

                try {
                    List<Map<String, Comparable>> entityObjs = entityRepository.listEntities(appId, entityType,
                            authUserId, skipValue, limitValue, sort, false, filters);

                    JSONObject responseBody = new JSONObject();
                    JSONObject entitiesJSONObject = new JSONObject();
                    entitiesJSONObject.put("results", entityObjs);
                    entitiesJSONObject.put("skip", skipValue);
                    entitiesJSONObject.put("limit", limitValue);
                    responseBody.put("entities", entitiesJSONObject);

                    return success(responseBody);
                } catch (Exception e) {
                    e.printStackTrace();
                    return serverError();
                }
            }

        } catch (EntityRemovedInDatabaseException e) {
            e.printStackTrace();
            return notFound();
        } catch (Exception e) {
            e.printStackTrace();
            return serverError();
        }
    }

    @Override
    public Representation deleteEntities() {
        try {
            if(isMaster()) {
                boolean status = entityRepository.deleteEntities(appId, entityType);
                if(status) {
                    pubSubService.deletedAll(appId, entityType);
                    return success();
                } else {
                    return badRequest();
                }
            } else {
                return unauthorized();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return serverError();
        }
    }

}
