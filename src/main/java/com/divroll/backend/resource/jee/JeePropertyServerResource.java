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
public class JeePropertyServerResource extends BaseServerResource
    implements PropertyResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeePropertyServerResource.class);

    @Inject
    EntityRepository entityRepository;

    @Override
    public void deleteProperty(Representation representation) {
        if(propertyName == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        if(Keys.isReservedPropertyKey(propertyName)) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        if(isMaster()) {
            boolean isDeleted = entityRepository.deleteProperty(appId, namespace, entityType, propertyName);
            if(isDeleted) {
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
        if(!isMaster()) {
            return unauthorized();
        }
        try {
            boolean updated = entityRepository.updateProperty(appId, namespace, entityType, propertyName,
                    new EntityMetadataBuilder()
                            .uniqueProperties(uniqueProperties)
                            .build());
            if(updated) {
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
