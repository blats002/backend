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
import com.divroll.domino.helper.JSON;
import com.divroll.domino.helper.ObjectLogger;
import com.divroll.domino.model.Application;
import com.divroll.domino.repository.EntityRepository;
import com.divroll.domino.resource.EntitiesResource;
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
public class JeeEntitiesServerResource extends BaseServerResource
        implements EntitiesResource {

    private static final Integer DEFAULT_LIMIT = 100;

    @Inject
    EntityRepository entityRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Representation createEntity(Representation entity) {
        JSONObject result = new JSONObject();
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entity == null || entity.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }
            String dir = appId;
            if (dir != null) {

                JSONObject jsonObject = JSONObject.parseObject(entity.getText());
                JSONObject _entityObject = jsonObject.getJSONObject("entity");

                if(_entityObject == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    return null;
                }

                Map<String, Comparable> comparableMap = JSON.toComparableMap(_entityObject);

                String[] read = new String[]{};
                String[] write = new String[]{};

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

                ObjectLogger.LOG(comparableMap);
                ObjectLogger.LOG(jsonObject);
                if (!comparableMap.isEmpty()) {

                    if(comparableMap.get("publicRead") != null) {
                        Comparable publicReadComparable = comparableMap.get("publicRead");
                        if(publicReadComparable instanceof Boolean) {
                            publicRead = (Boolean) publicReadComparable;
                        } else if(publicReadComparable instanceof String) {
                            publicRead = Boolean.valueOf((String) publicReadComparable);
                        }
                     }


                    if(comparableMap.get("publicWrite") != null) {
                        Comparable publicWriteComparable = comparableMap.get("publicWrite");
                        if(publicWriteComparable instanceof Boolean) {
                            publicWrite = (Boolean) publicWriteComparable;
                        } else if(publicWriteComparable instanceof String) {
                            publicWrite = Boolean.valueOf((String) publicWriteComparable);
                        }
                    }

                    String entityId = entityRepository.createEntity(appId, entityType, comparableMap, read, write, publicRead, publicWrite);
                    JSONObject entityObject = new JSONObject();
                    entityObject.put(Constants.ENTITY_ID, entityId);
                    result.put("entity", entityObject);
                    setStatus(Status.SUCCESS_CREATED);
                } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        Representation representation = null;
        if (result != null) {
            representation = new JsonRepresentation(result.toJSONString());
        }
        return representation;
    }

    @Override
    public Representation getEntities() {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            Application app = applicationService.read(appId);
            if (app == null) {
                return null;
            }

            int skipValue = 0;
            int limitValue = DEFAULT_LIMIT;

            if(skip != null && limit != null) {
                skipValue = skip;
                limitValue = limit;
            }

            if (isMaster(appId, masterKey)) {
                try {
                    List<Map<String, Object>> entityObjs
                            = entityRepository.listEntities(appId, entityType, null,
                            skipValue, limitValue, sort, true);
                    JSONObject responseBody = new JSONObject();
                    JSONObject entitiesJSONObject = new JSONObject();
                    entitiesJSONObject.put("results", entityObjs);
                    entitiesJSONObject.put("skip", skipValue);
                    entitiesJSONObject.put("limit", limitValue);
                    responseBody.put("entities", entitiesJSONObject);
                    Representation representation = new JsonRepresentation(responseBody.toJSONString());                    setStatus(Status.SUCCESS_OK);
                    return representation;
                } catch (Exception e) {
                    setStatus(Status.SERVER_ERROR_INTERNAL);
                }
            } else {

                String authUserId = null;

                try {
                    authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                } catch (Exception e) {
                    // do nothing
                }

                try {
                    List<Map<String, Object>> entityObjs = entityRepository.listEntities(appId, entityType,
                            authUserId, skipValue, limitValue, sort, false);
                    JSONObject responseBody = new JSONObject();
                    JSONObject entitiesJSONObject = new JSONObject();
                    entitiesJSONObject.put("results", entityObjs);
                    entitiesJSONObject.put("skip", skipValue);
                    entitiesJSONObject.put("limit", limitValue);
                    responseBody.put("entities", entitiesJSONObject);
                    Representation representation = new JsonRepresentation(responseBody.toJSONString());
                    setStatus(Status.SUCCESS_OK);
                    return representation;
                } catch (Exception e) {
                    setStatus(Status.SERVER_ERROR_INTERNAL);
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
}
