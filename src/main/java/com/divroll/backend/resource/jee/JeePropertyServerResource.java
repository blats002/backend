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

import com.divroll.backend.model.Keys;
import com.divroll.backend.model.builder.EntityMetadataBuilder;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.resource.PropertyResource;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeePropertyServerResource extends BaseServerResource implements PropertyResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeePropertyServerResource.class);

  @Inject EntityRepository entityRepository;

  @Override
  public void deleteProperty(Representation representation) {
    if (propertyName == null) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return;
    }
    if (Keys.isReservedPropertyKey(propertyName)) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return;
    }
    if (isMaster()) {
      boolean isDeleted =
          entityRepository.deleteProperty(appId, namespace, entityType, propertyName);
      if (isDeleted) {
        setStatus(Status.SUCCESS_OK);
      } else {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      }
    } else {
      setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }
  }

  @Override
  public Representation updateProperty(Representation entity) {
    if (!isMaster()) {
      return unauthorized();
    }
    if (propertyName == null) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return null;
    }
    if (Keys.isReservedPropertyKey(propertyName)) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return null;
    }
    try {
      boolean updated =
          entityRepository.updateProperty(
              appId,
              namespace,
              entityType,
              propertyName,
              new EntityMetadataBuilder().uniqueProperties(uniqueProperties).build());
      if (updated) {
        setStatus(Status.SUCCESS_OK);
      } else {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      }
    } catch (Exception e) {
      return serverError();
    }
    return null;
  }
}
