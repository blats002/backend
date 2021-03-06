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

  @Inject XodusStore store;

  @Inject EntityRepository entityRepository;

  @Override
  public String createFunction(String appId, String namespace, String functionName, String jar) {
    Map<String, Comparable> comparableMap = new LinkedHashMap<>();
    comparableMap.put("appId", appId);
    comparableMap.put("functionName", functionName);
    comparableMap.put("jar", jar);
    EntityId entityId =
        store.putIfNotExists(
            masterStore, namespace, Constants.ENTITYSTORE_FUNCTION, comparableMap, "functionName");
    return entityId.toString();
  }

  @Override
  public boolean deleteFunction(String appId, String namespace, String functionName) {
    try {
      store.delete(masterStore, Constants.ENTITYSTORE_FUNCTION, "functionName", functionName);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public byte[] retrieveFunction(String appId, String namespace, String functionName) {
    EntityId entityId =
        store.getFirstEntityId(
            masterStore,
            namespace,
            Constants.ENTITYSTORE_FUNCTION,
            "functionName",
            functionName,
            String.class);
    Map<String, Comparable> comparableMap = store.get(masterStore, namespace, entityId);
    String jar = (String) comparableMap.get("jar");
    return Base64.base64ToByteArray(jar);
  }

  @Override
  public InputStream retrieveFunctionEntity(String appId, String namespace, String functionName) {
    System.out.println("appId = " + appId);
    InputStream is =
        entityRepository.getFirstEntityBlob(
            appId,
            namespace,
            Constants.ENTITYSTORE_FUNCTION,
            "functionName",
            functionName,
            String.class,
            "jar");
    return is;
  }
}
