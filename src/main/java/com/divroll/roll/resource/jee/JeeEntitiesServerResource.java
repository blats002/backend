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

import com.divroll.roll.Constants;
import com.divroll.roll.helper.ACLHelper;
import com.divroll.roll.helper.JSON;
import com.divroll.roll.helper.ObjectLogger;
import com.divroll.roll.model.Application;
import com.divroll.roll.model.EmbeddedArrayIterable;
import com.divroll.roll.repository.EntityRepository;
import com.divroll.roll.resource.EntitiesResource;
import com.divroll.roll.service.WebTokenService;
import com.google.inject.Inject;
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

                JSONObject jsonObject = new JSONObject(entity.getText());
                JSONObject entityJSONObject = jsonObject.getJSONObject("entity");

                if (entityJSONObject == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    return null;
                }

                Map<String, Comparable> comparableMap = JSON.toComparableMap(entityJSONObject);

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

                    //System.out.println("READ: " + ((EmbeddedArrayIterable) comparableMap.get(Constants.ACL_READ)).asJSONArray());
                    //System.out.println("WRITE: " + ((EmbeddedArrayIterable) comparableMap.get(Constants.ACL_WRITE)).asJSONArray());

                    if(comparableMap.get(Constants.ACL_READ) != null) {
                        EmbeddedArrayIterable iterable = (EmbeddedArrayIterable) comparableMap.get(Constants.ACL_READ);
                        JSONArray jsonArray = iterable.asJSONArray();
                        read = ACLHelper.onlyIds(jsonArray);
                    }

                    if(comparableMap.get(Constants.ACL_WRITE) != null) {
                        EmbeddedArrayIterable iterable = (EmbeddedArrayIterable) comparableMap.get(Constants.ACL_WRITE);
                        JSONArray jsonArray = iterable.asJSONArray();
                        write = ACLHelper.onlyIds(jsonArray);
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
            representation = new JsonRepresentation(result.toString());
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

            if (skip != null && limit != null) {
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
                    Representation representation = new JsonRepresentation(responseBody.toString());
                    setStatus(Status.SUCCESS_OK);
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
                    Representation representation = new JsonRepresentation(responseBody.toString());
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