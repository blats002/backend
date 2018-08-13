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
package com.divroll.domino.service.jee;

import com.divroll.domino.model.Application;
import com.divroll.domino.service.ApplicationService;
import com.divroll.domino.xodus.XodusStore;
import com.google.inject.Inject;
import jetbrains.exodus.entitystore.EntityId;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeApplicationService implements ApplicationService {

    private static final String KIND = "Application";
    private static final String DIR = "master";

    @Inject
    XodusStore store;

    @Override
    public EntityId create(Application application) {
//        Key entityId = applicationRepository.save(application, Application.class);
//        return entityId;
        Map<String, Comparable> comparableMap = new LinkedHashMap<>();
        comparableMap.put("masterKey", application.getMasterKey());
        comparableMap.put("apiKey", application.getApiKey());
        comparableMap.put("appId", application.getAppId());
        EntityId entityId = store.put(DIR, KIND, comparableMap);
        return entityId;
    }

    @Override
    public Application read(String applicationId) {
        EntityId id = store.getFirstEntityId(DIR, KIND, "appId", applicationId, String.class);
        if (id != null) {
            Map<String, Comparable> entityMap = store.get(DIR, id.toString());
            if (entityMap != null) {
                Application application = new Application();
                application.setAppId((String) entityMap.get("appId"));
                application.setApiKey((String) entityMap.get("apiKey"));
                application.setMasterKey((String) entityMap.get("masterKey"));
                return application;
            }
        }
        return null;
    }

    @Override
    public void update(Application application, String theMasterKey) {
        Map<String, Comparable> comparableMap = new LinkedHashMap<>();
        comparableMap.put("appId", application.getAppId());
        comparableMap.put("apiKey", application.getApiKey());
        comparableMap.put("masterKey", application.getMasterKey());
        EntityId entityId = store.getFirstEntityId(DIR, KIND, "masterKey", theMasterKey, String.class);
        store.update(DIR, KIND, entityId.toString(), comparableMap);
    }

    @Override
    public void delete(String entityId) {
        store.delete(DIR, KIND, entityId);
    }

}
