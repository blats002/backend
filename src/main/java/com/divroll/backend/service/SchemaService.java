package com.divroll.backend.service;

import com.divroll.backend.model.EntityPropertyType;
import com.divroll.backend.model.EntityType;

import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface SchemaService {
  List<EntityType> listSchemas(String appId, String namespace);

  List<EntityPropertyType> listPropertyTypes(String appId, String entityType, String namespace);
}
