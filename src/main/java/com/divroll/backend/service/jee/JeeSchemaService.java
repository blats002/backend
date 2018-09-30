package com.divroll.backend.service.jee;

import com.divroll.backend.model.EntityPropertyType;
import com.divroll.backend.model.EntityType;
import com.divroll.backend.service.SchemaService;
import com.divroll.backend.xodus.XodusStore;
import com.google.inject.Inject;

import java.util.LinkedList;
import java.util.List;

public class JeeSchemaService implements SchemaService {

    @Inject
    XodusStore store;

    @Override
    public List<EntityType> listSchemas(String appId) {
        List<EntityType> entityTypeList = new LinkedList<>();
        store.listEntityTypes(appId).forEach(s -> {
            EntityType entityType = new EntityType();
            entityType.setEntityType(s);
            entityType.setPropertyTypes(store.listPropertyTypes(appId, s));
            entityTypeList.add(entityType);
        });
        return entityTypeList;
    }

    @Override
    public List<EntityPropertyType> listPropertyTypes(String appId, String entityType) {
        List<EntityPropertyType> propertyTypes = new LinkedList<>();
        listSchemas(appId).forEach(schema -> {
            if(schema.getEntityType().equalsIgnoreCase(entityType)) {
                propertyTypes.addAll(schema.getPropertyTypes());
            }
        });
        return propertyTypes;
    }
}
