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
package com.divroll.backend.repository.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.FunctionRepository;
import com.divroll.backend.util.Base64;
import com.divroll.backend.xodus.XodusStore;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityId;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeFunctionRepository implements FunctionRepository {

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    @Named("masterStore")
    String masterStore;

    @Inject
    XodusStore store;

    @Inject
    EntityRepository entityRepository;

    @Override
    public String createFunction(String appId, String functionName, String jar) {
        Map<String, Comparable> comparableMap = new LinkedHashMap<>();
        comparableMap.put("appId", appId);
        comparableMap.put("functionName", functionName);
        comparableMap.put("jar", jar);
        EntityId entityId = store.putIfNotExists(masterStore, Constants.ENTITYSTORE_FUNCTION, comparableMap, "functionName");
        return entityId.toString();
    }

    @Override
    public boolean deleteFunction(String appId, String functionName) {
        try {
            store.delete(masterStore, Constants.ENTITYSTORE_FUNCTION, "functionName", functionName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public byte[] retrieveFunction(String appId, String functionName) {
        EntityId entityId = store.getFirstEntityId(masterStore, Constants.ENTITYSTORE_FUNCTION, "functionName", functionName, String.class);
        Map<String, Comparable> comparableMap = store.get(masterStore, entityId);
        String jar = (String) comparableMap.get("jar");
        return Base64.base64ToByteArray(jar);
    }

    @Override
    public byte[] retrieveFunctionEntity(String appId, String functionName) {
        byte[] jarBytes = null;
        System.out.println("appId = " + appId);
        InputStream is = entityRepository.getFirstEntityBlob(appId, Constants.ENTITYSTORE_FUNCTION,
                "functionName", functionName, String.class, "jar");
        try {
            jarBytes = ByteStreams.toByteArray(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jarBytes;
    }
}