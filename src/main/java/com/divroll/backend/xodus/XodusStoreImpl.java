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
package com.divroll.backend.xodus;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class XodusStoreImpl implements XodusStore {

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    XodusManager manager;

    /*
    @Override
    public void putIfNotExists(String dir, final String entityType, final String propertyKey, final String propertyValue) {
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + dir);
        entityStore.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
                final Entity entity = txn.newEntity(entityType);
                entity.setProperty(propertyKey, propertyValue);
            }
        });
        entityStore.close();
    }
    @Override
    public void putIfNotExists(String dir, final String entityType, final String propertyKey, final Double propertyValue) {
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + dir);
        entityStore.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
                final Entity entity = txn.newEntity(entityType);
                entity.setProperty(propertyKey, propertyValue);
            }
        });
        entityStore.close();
    }
    @Override
    public void putIfNotExists(String dir, final String entityType, final String propertyKey, final Long propertyValue) {
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + dir);
        entityStore.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
                final Entity entity = txn.newEntity(entityType);
                EntityId id = entity.getId();
                entity.setProperty(propertyKey, propertyValue);
            }
        });
        entityStore.close();
    }
    @Override
    public void putIfNotExists(String dir, final String entityType, final String propertyKey, final Boolean propertyValue) {
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + dir);
        entityStore.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
                final Entity entity = txn.newEntity(entityType);
                entity.setProperty(propertyKey, propertyValue);
            }
        });
        entityStore.close();
    }


    @Override
    public EntityId putIfNotExists(String dir, final String entityType, final String propertyKey, final InputStream is) {
        final EntityId[] entityId = {null};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + dir);
        entityStore.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
                final Entity entity = txn.newEntity(entityType);
                entity.setBlob(propertyKey, is);
                entityId[0] = entity.getId();
            }
        });
        entityStore.close();
        return entityId[0];
    }
    */

    @Override
    public EntityId put(String dir, final String kind, final Map<String, Comparable> properties) {
        if (dir == null || kind == null) {
            return null;
        }
        final EntityId[] entityId = {null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final Entity entity = txn.newEntity(kind);
                    Iterator<String> it = properties.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        Comparable comparable = properties.get(key);
                        entity.setProperty(key, comparable);
                    }
                    entityId[0] = entity.getId();
                }
            });
        } finally {
            //entityStore.close();
        }
        return entityId[0];
    }

    @Override
    public <T> EntityId put(String dir, final String kind, final String id,
                            final Map<String, Comparable> properties) {
        final EntityId[] result = {null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId entityId = txn.toEntityId(id);
                    Entity entity = txn.getEntity(entityId);
                    Iterator<String> it = properties.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        Comparable comparable = properties.get(key);
                        entity.setProperty(key, comparable);
                    }
                    result[0] = entityId;
                }
            });
        } finally {
            //entityStore.close();
        }
        return result[0];
    }

    @Override
    public byte[] getBlob(final String dir, final String kind, final String blobKey) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        final List<Comparable<InputStream>> results = new LinkedList<Comparable<InputStream>>();
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final Entity entity = txn.newEntity(kind);
                    InputStream is = entity.getBlob(blobKey);
                    results.add((Comparable<InputStream>) is);
                }
            });
        } finally {
            //entityStore.close();
        }
        try {
            InputStream is = (InputStream) ((LinkedList<Comparable<InputStream>>) results).getFirst();
            byte[] byteArray = ByteStreams.toByteArray(is);
            return byteArray;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public EntityId update(final String dir, final String kind, final String id,
                           final Map<String, Comparable> properties) {
        final EntityId[] entityId = {null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    entityId[0] = txn.toEntityId(id);
                    Entity entity = txn.getEntity(entityId[0]);
                    Iterator<String> it = properties.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        Comparable comparable = properties.get(key);
                        entity.setProperty(key, comparable);
                    }
                }
            });
        } finally {
            //entityStore.close();
        }
        return entityId[0];
    }

    @Override
    public Map<String, Comparable> get(String dir, final String id) {
        final Map<String, Comparable>[] result = new Map[]{null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId entityId = txn.toEntityId(id);
                    Entity entity = txn.getEntity(entityId);
                    result[0] = new LinkedHashMap<>();
                    List<String> props = entity.getPropertyNames();
                    for (String prop : props) {
                        result[0].put(prop, entity.getProperty(prop));
                    }
                }
            });
        } finally {
            //entityStore.close();
        }
        return result[0];
    }

    @Override
    public Map<String, Comparable> get(String dir, EntityId id) {
        final Map<String, Comparable>[] result = new Map[]{null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    Entity entity = txn.getEntity(id);
                    result[0] = new LinkedHashMap<>();
                    List<String> props = entity.getPropertyNames();
                    for (String prop : props) {
                        result[0].put(prop, entity.getProperty(prop));
                    }
                }
            });
        } finally {
            //entityStore.close();
        }
        return result[0];
    }

    @Override
    public <T> T get(String dir, String kind, String id, String key) {
        return null;
    }

    @Override
    public void delete(String dir, final String id) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    // TODO
                }
            });
        } finally {
            //entityStore.close();
        }
    }

    @Override
    public void delete(String dir, final String... ids) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    for (String p : ids) {
                        // TODO
                    }
                }
            });
        } finally {
            //entityStore.close();
        }
    }


    @Override
    public <T> EntityId getFirstEntityId(String dir, final String kind, final String propertyKey, final Comparable<T> propertyVal,
                                         Class<T> clazz) {
        final EntityId[] entityId = {null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    Entity entity = txn.find(kind, propertyKey, propertyVal).getFirst();
                    if (entity != null) {
                        entityId[0] = entity.getId();
                    }
                }
            });
        } finally {
            //entityStore.close();
        }
        return entityId[0];
    }

    @Override
    public List<Map<String, Comparable>> list(String dir, final String entityType, int skip, int limit) {
        List<Map<String, Comparable>> list = new LinkedList<Map<String, Comparable>>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityIterable result = txn.getAll(entityType);
                    result = result.skip(skip).take(limit);
                    for(Entity entity : result) {
                        Map<String, Comparable> map = new LinkedHashMap<>();
                        List<String> props = entity.getPropertyNames();
                        for (String prop : props) {
                            map.put(prop, entity.getProperty(prop));
                        }
                        list.add(map);
                    }
                }
            });
        } finally {
            //entityStore.close();
        }
        return list;
    }

    @Override
    public List<String> listEntityTypes(String dir) {
        List<String> list = new LinkedList<String>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    List<String> entityTypes = txn.getEntityTypes();
                    if(entityTypes != null) {
                        for(String entityType : entityTypes) {
                            list.add(entityType);
                        }
                    }
                }
            });
        } finally {
            //entityStore.close();
        }
        return list;
    }


}
