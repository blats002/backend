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
package com.divroll.backend.service.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.*;
import com.divroll.backend.service.ApplicationService;
import com.divroll.backend.xodus.XodusStore;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityId;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeApplicationService implements ApplicationService {

    @Inject
    @Named("masterStore")
    String masterStore;

    @Inject
    XodusStore store;

    @Override
    public EntityId create(Application application) {
        Map<String, Comparable> comparableMap = new LinkedHashMap<>();
        comparableMap.put(Constants.MASTER_KEY, application.getMasterKey());
        comparableMap.put(Constants.API_KEY, application.getApiKey());
        comparableMap.put(Constants.APP_ID, application.getAppId());
        if(application.getAppName() != null) {
            comparableMap.put(Constants.APP_NAME, application.getAppName());
        }
        EntityId entityId = store.put(masterStore, Constants.ENTITYSTORE_APPLICATION, comparableMap);
        return entityId;
    }

    @Override
    public Application read(String applicationId) {
        EntityId id = store.getFirstEntityId(masterStore, Constants.ENTITYSTORE_APPLICATION, Constants.APP_ID,
                applicationId, String.class);
        if (id != null) {
            Map<String, Comparable> entityMap = store.get(masterStore, id.toString());
            if (entityMap != null) {
                Application application = new Application();
                application.setAppId((String) entityMap.get(Constants.APP_ID));
                application.setApiKey((String) entityMap.get(Constants.API_KEY));
                application.setMasterKey((String) entityMap.get(Constants.MASTER_KEY));
                application.setAppName((String) entityMap.get(Constants.APP_NAME));
                EmbeddedArrayIterable schemas = (EmbeddedArrayIterable) entityMap.get("schemas");
                JSONArray schemasArray = schemas != null ? schemas.asJSONArray() : null;
                if(schemasArray != null) {
                    List<Schema> schemaList = new LinkedList<>();
                    for(int i=0;i<schemasArray.length();i++) {
                        JSONObject schemaObj = schemasArray.getJSONObject(i);
                        String entityType = schemaObj.getString("entityType");
                        JSONArray propertyTypes = schemaObj.getJSONArray("propertyTypes");
                        SchemaPropertyList schemaPropertyList = new SchemaPropertyList();
                        for(int j=0;j<propertyTypes.length();j++) {
                            JSONObject propertyTypeObj = propertyTypes.getJSONObject(j);
                            String pName = propertyTypeObj.getString("propertyName");
                            String pType = propertyTypeObj.getString("propertyType");
                            SchemaProperty schemaProperty = new SchemaProperty(pName, pType);
                            schemaPropertyList.add(schemaProperty);
                        }
                        Schema schema = new Schema();
                        schema.setEntityType(entityType);
                        schema.setSchemaProperties(schemaPropertyList);
                        schemaList.add(schema);
                    }
                    application.setSchemas(schemaList);
                } else {
                    // TODO:
                }

                System.out.println("--->" + new Gson().toJson(application));
                return application;
            }
        }
        return null;
    }

    @Override
    public void update(Application application, String theMasterKey) {
        Map<String, Comparable> comparableMap = new LinkedHashMap<>();
        comparableMap.put(Constants.APP_ID, application.getAppId());
        comparableMap.put(Constants.API_KEY, application.getApiKey());
        comparableMap.put(Constants.MASTER_KEY, application.getMasterKey());
        if(application.getAppName() != null) {
            comparableMap.put(Constants.APP_NAME, application.getAppName());
        }
        EntityId entityId = store.getFirstEntityId(masterStore, Constants.ENTITYSTORE_APPLICATION,
                Constants.MASTER_KEY, theMasterKey, String.class);
        store.update(masterStore, Constants.ENTITYSTORE_APPLICATION, entityId.toString(), comparableMap);
    }

    @Override
    public void delete(String entityId) {
        store.delete(masterStore, Constants.ENTITYSTORE_APPLICATION, entityId);
    }

    @Override
    public List<Application> list(int skip, int limit) {
        List<Application> apps = new LinkedList<>();
        List<Map<String,Comparable>> list = store.list(masterStore, Constants.ENTITYSTORE_APPLICATION, skip, limit);
        for(Map entityMap : list) {
            if (entityMap != null) {
                Application application = new Application();
                application.setAppId((String) entityMap.get(Constants.APP_ID));
                application.setApiKey((String) entityMap.get(Constants.API_KEY));
                application.setMasterKey((String) entityMap.get(Constants.MASTER_KEY));
                application.setAppName((String) entityMap.get(Constants.APP_NAME));
                apps.add(application);
            }
        }
        return apps;
    }

    @Override
    public void forceUpdate(Application application) {
        Map<String, Comparable> comparableMap = new LinkedHashMap<>();
        comparableMap.put(Constants.APP_ID, application.getAppId());
        comparableMap.put(Constants.API_KEY, application.getApiKey());
        comparableMap.put(Constants.MASTER_KEY, application.getMasterKey());
        if(application.getAppName() != null) {
            comparableMap.put(Constants.APP_NAME, application.getAppName());
        }
        List<Schema> schemas = application.getSchemas();
        if(schemas != null && !schemas.isEmpty()) {
            JSONArray schemaJsa = new JSONArray();
            schemas.forEach(schema -> {
                JSONObject schemaObj = new JSONObject();
                String entityType = schema.getEntityType();
                JSONArray propArray = new JSONArray();
                schema.getSchemaProperties().forEach(schemaProperty -> {
                    String pName = schemaProperty.getPropertyName();
                    SchemaProperty.TYPE pType = schemaProperty.getPropertyType();
                    JSONObject propObj = new JSONObject();
                    propObj.put("propertyName", pName);
                    propObj.put("propertyType", pType);
                    propArray.put(propObj);
                });
                schemaObj.put("entityType", entityType);
                schemaObj.put("propertyTypes", propArray);
                schemaJsa.put(schemaObj);
            });
            System.out.println("--------------------->" + schemaJsa);
            comparableMap.put("schemas", new EmbeddedArrayIterable(schemaJsa));
        }
        EntityId entityId = store.getFirstEntityId(masterStore, Constants.ENTITYSTORE_APPLICATION,
                Constants.APP_ID, application.getAppId(), String.class);
        EntityId id = store.update(masterStore, Constants.ENTITYSTORE_APPLICATION, entityId.toString(), comparableMap);
        System.out.println("Updated: " + id);
//        Map<String,Comparable> resultMap = store.get(Constants.ENTITYSTORE_APPLICATION, id.toString());
//        System.out.println("->" + resultMap);
    }

}
