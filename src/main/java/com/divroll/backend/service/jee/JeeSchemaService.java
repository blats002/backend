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

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeSchemaService.class);

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
