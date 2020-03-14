/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.service.jee;

import com.divroll.backend.model.EntityPropertyType;
import com.divroll.backend.model.EntityType;
import com.divroll.backend.service.SchemaService;
import com.divroll.backend.xodus.XodusStore;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeSchemaService implements SchemaService {

  private static final Logger LOG = LoggerFactory.getLogger(JeeSchemaService.class);

  @Inject XodusStore store;

  @Override
  public List<EntityType> listSchemas(String appId, String namespace) {
    List<EntityType> entityTypeList = new LinkedList<>();
    store
        .listEntityTypes(appId, namespace)
        .forEach(
            s -> {
              EntityType entityType = new EntityType();
              entityType.setEntityType(s);
              entityType.setPropertyTypes(store.listPropertyTypes(appId, namespace, s));
              entityTypeList.add(entityType);
            });
    return entityTypeList;
  }

  @Override
  public List<EntityPropertyType> listPropertyTypes(
      String appId, String namespace, String entityType) {
    List<EntityPropertyType> propertyTypes = new LinkedList<>();
    listSchemas(appId, namespace)
        .forEach(
            schema -> {
              if (schema.getEntityType().equalsIgnoreCase(entityType)) {
                propertyTypes.addAll(schema.getPropertyTypes());
              }
            });
    return propertyTypes;
  }
}
