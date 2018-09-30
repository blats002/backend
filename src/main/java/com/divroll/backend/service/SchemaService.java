package com.divroll.backend.service;

import com.divroll.backend.model.EntityPropertyType;
import com.divroll.backend.model.EntityType;

import java.util.List;

public interface SchemaService {
    List<EntityType> listSchemas(String appId);
    List<EntityPropertyType> listPropertyTypes(String appId, String entityType);
}
