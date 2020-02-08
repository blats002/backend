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
 */
package com.divroll.backend.xodus.impl;

import com.divroll.backend.model.EmbeddedArrayIterable;
import com.divroll.backend.model.EmbeddedEntityBinding;
import com.divroll.backend.model.EmbeddedEntityIterable;
import com.divroll.backend.xodus.XodusManager;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.EnvironmentConfig;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.vfs.VirtualFileSystem;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class XodusManagerImpl implements XodusManager {

  private static final Logger LOG = LoggerFactory.getLogger(XodusManagerImpl.class);

  Map<String, Environment> environmentMap = new LinkedHashMap<>();
  Map<String, PersistentEntityStore> entityStoreMap = new LinkedHashMap<>();
  VirtualFileSystem virtualFileSystem = null;

  @Override
  public Environment getEnvironment(String xodusRoot, String instance) {
    Environment environment = environmentMap.get(xodusRoot + instance);
    if (environment == null) {
      EnvironmentConfig config = new EnvironmentConfig();
      //config.setLogDataReaderWriterProvider("jetbrains.exodus.log.replication.S3DataReaderWriterProvider");
      //config.setLogCacheShared(false);
      Environment env = Environments.newInstance(xodusRoot + instance, config);
      environmentMap.put(xodusRoot + instance, env);
    }
    Environment e = environmentMap.get(xodusRoot + instance);
    return e;
  }

  @Override
  public PersistentEntityStore getPersistentEntityStore(String xodusRoot, String dir) {
    PersistentEntityStore entityStore = entityStoreMap.get(xodusRoot + dir);
    if (entityStore == null) {
      Environment environment = getEnvironment(xodusRoot, dir);
      final PersistentEntityStore store = PersistentEntityStores.newInstance(environment);
      store.getConfig().setDebugSearchForIncomingLinksOnDelete(true);
      //store.getConfig().setRefactoringHeavyLinks(true);
      store.executeInTransaction(
              txn -> {
                store.registerCustomPropertyType(
                    txn, EmbeddedEntityIterable.class, EmbeddedEntityBinding.BINDING);
                store.registerCustomPropertyType(
                    txn, EmbeddedArrayIterable.class, EmbeddedEntityBinding.BINDING);
              });
      entityStoreMap.put(xodusRoot + dir, store);
    }
    PersistentEntityStore p = entityStoreMap.get(xodusRoot + dir);
    return p;
  }

  @Override
  public VirtualFileSystem getVirtualFileSystem(Environment env) {
    if (virtualFileSystem == null) {
      virtualFileSystem = new VirtualFileSystem(env);
    }
    return virtualFileSystem;
  }
}
