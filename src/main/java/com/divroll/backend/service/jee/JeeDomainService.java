/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
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
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * GPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.service.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.Domain;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.service.DomainService;
import com.divroll.backend.xodus.XodusStore;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityId;

import java.util.Map;

public class JeeDomainService implements DomainService {

    @Inject
    @Named("masterStore")
    String masterStore;

    @Inject
    XodusStore store;

    @Override
    public Domain retrieveDomain(String domainName) {
        EntityId id =
                store.getFirstEntityId(
                        masterStore,
                        null,
                        Constants.ENTITYSTORE_DOMAIN,
                        Constants.DOMAIN_NAME,
                        domainName,
                        String.class);
        if (id != null) {
            Map<String, Comparable> entityMap = store.get(masterStore, null, id.toString());
            String appName = (String) entityMap.get(Constants.APP_NAME);
            String superuserId = (String) entityMap.get(Constants.SUPERUSER);
            Superuser superuser = new Superuser();
            superuser.setEntityId(superuserId);
            if(entityMap != null) {
                Domain domain = new Domain(id.toString(), domainName, appName, superuser);
                return domain;
            }
        }
        return null;
    }
}
