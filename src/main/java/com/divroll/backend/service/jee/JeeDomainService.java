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
