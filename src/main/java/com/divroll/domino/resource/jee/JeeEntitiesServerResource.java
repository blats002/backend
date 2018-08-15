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
import com.divroll.domino.repository.EntityRepository;
import com.divroll.domino.resource.EntitiesResource;
import com.google.inject.Inject;
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

    @Inject
    EntityRepository entityRepository;

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
                Map<String,Comparable> comparableMap = new LinkedHashMap<>();
                while(it.hasNext()) {
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

                String[] read = new String[]{"*"};
                String[] write = new String[]{"*"};

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

                if(!comparableMap.isEmpty()) {
                    String entityId = entityRepository.createEntity(appId, kind, comparableMap, read, write);
                    JSONObject entityObject = new JSONObject();
                    entityObject.put("entityId", entityId);
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
        if(result != null) {
           representation = new JsonRepresentation(result.toJSONString());
        }
        return representation;
    }

    @Override
    public Representation getEntities() {
        return null;
    }
}
