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
