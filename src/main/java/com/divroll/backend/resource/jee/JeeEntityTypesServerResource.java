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
package com.divroll.backend.resource.jee;

import com.divroll.backend.model.EntityType;
import com.divroll.backend.model.EntityTypes;
import com.divroll.backend.resource.EntityTypesResource;
import com.divroll.backend.xodus.XodusStore;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import org.restlet.data.Status;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeEntityTypesServerResource extends BaseServerResource
    implements EntityTypesResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeEntityTypesServerResource.class);

  @Inject XodusStore store;

  @Override
  public EntityTypes getEntityTypes() {
    try {
      EntityTypes entityTypes = new EntityTypes();
      if (appId == null || appId.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing App ID");
        return null;
      }
      if (isMaster()) {
        entityTypes.setLimit(0);
        entityTypes.setSkip(0);

        List<EntityType> entityTypeList = new LinkedList<>();
        store
            .listEntityTypes(appId, null)
            .forEach(
                s -> {
                  EntityType entityType = new EntityType();
                  entityType.setEntityType(s);
                  entityType.setPropertyTypes(store.listPropertyTypes(appId, null, s));
                  entityTypeList.add(entityType);
                });

        entityTypes.setResults(entityTypeList);
        setStatus(Status.SUCCESS_OK);
        return entityTypes;
      } else {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return null;
  }
}
