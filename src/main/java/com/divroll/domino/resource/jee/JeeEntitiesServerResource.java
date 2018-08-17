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
                    String entityId = entityRepository.createEntity(appId, entityType, comparableMap, read, write);
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
            if (entityId == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing entity ID in request");
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
                            = entityRepository.listEntities(appId, entityType, skipValue, limitValue);
                    Representation representation = new JsonRepresentation(entityObjs);
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

                Boolean publicRead = false;
                Boolean isAccess = false;

                List<Map<String, Object>> result = new LinkedList<>();

                try {
                    List<Map<String, Object>> entityObjs = entityRepository.listEntities(appId, entityType, skipValue, limitValue);
                    for(Map<String, Object> entityObj : entityObjs) {
                        List<String> aclReadList = (List<String>) ((Map<String, Object>) entityObj.get("_md")).get("aclRead");
                        if (aclReadList.contains(Constants.ACL_ASTERISK)) {
                            publicRead = true;
                        } else if (authUserId != null && aclReadList.contains(authUserId)) {
                            isAccess = true;
                        }
                        if (publicRead || isAccess) {
                            result.add(entityObj);
                        }
                    }
                    Representation representation = new JsonRepresentation(result);
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
        return null;    }
}
