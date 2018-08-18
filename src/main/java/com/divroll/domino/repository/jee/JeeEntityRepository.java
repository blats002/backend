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
package com.divroll.domino.repository.jee;

import com.divroll.domino.Constants;
import com.divroll.domino.model.Role;
import com.divroll.domino.repository.EntityRepository;
import com.divroll.domino.repository.RoleRepository;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.*;
import org.jetbrains.annotations.NotNull;
import scala.actors.threadpool.Arrays;

import java.io.InputStream;
import java.util.*;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeEntityRepository implements EntityRepository {

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    @Named("defaultRoleStore")
    String defaultRoleStore;

    @Inject
    RoleRepository roleRepository;

    @Override
    public String createEntity(final String instance, final String storeName, final Map<String, Comparable> comparableMap,
                               final String[] read, final String[] write, final Boolean publicRead, final Boolean publicWrite) {

        final String[] entityId = {null};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final Entity entity = txn.newEntity(storeName);

                    Iterator<String> it = comparableMap.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        Comparable value = comparableMap.get(key);
                        entity.setProperty(key, value);
                    }

                    if (read != null) {
                        List<String> aclRead = Arrays.asList(read);
                        // Add User to ACL
                        for (String userOrRoleId : aclRead) {
                            EntityId userorRoleEntityId = txn.toEntityId(userOrRoleId);
                            Entity userOrRoleEntity = txn.getEntity(userorRoleEntityId);
                            if (userOrRoleEntity != null) {
                                entity.addLink(Constants.ACL_READ, userOrRoleEntity);
                            }
                        }
                    }

                    if (write != null) {
                        List<String> aclWrite = Arrays.asList(write);
                        // Add User to ACL
                        for (String userId : aclWrite) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userEntity = txn.getEntity(userEntityId);
                            if (userEntity != null) {
                                entity.addLink(Constants.ACL_WRITE, userEntity);
                            }
                        }
                    }

                    entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                    entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);


                    entityId[0] = entity.getId().toString();
                }
            });
        } finally {
            entityStore.close();
        }
        return entityId[0];
    }

    @Override
    public boolean updateEntity(String instance, String storeName, final String entityId, final Map<String, Comparable> comparableMap,
                                final String[] read, final String[] write, final Boolean publicRead, final Boolean publicWrite) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId roleEntityId = txn.toEntityId(entityId);

                    final Entity entity = txn.getEntity(roleEntityId);
                    Iterator<String> it = comparableMap.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        Comparable value = comparableMap.get(key);
                        entity.setProperty(key, value);
                    }

                    if (read != null) {
                        boolean publicRead = true;
                        List<String> aclRead = Arrays.asList(read);
                        // Add User to ACL
                        for (String userId : aclRead) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userEntity = txn.getEntity(userEntityId);
                            if (userEntity != null) {
                                entity.addLink(Constants.ACL_READ, userEntity);
                            }
                        }
                        entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                    }

                    if (write != null) {
                        boolean publicWrite = true;
                        List<String> aclWrite = Arrays.asList(write);
                        // Add User to ACL
                        for (String userId : aclWrite) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userEntity = txn.getEntity(userEntityId);
                            if (userEntity != null) {
                                entity.addLink(Constants.ACL_WRITE, userEntity);
                            }
                        }
                        entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
                    }

                    success[0] = true;
                }
            });
        } finally {
            entityStore.close();
        }
        return success[0];
    }

    @Override
    public Map<String, Object> getEntity(String instance, final String storeName, final String entityId) {
        final Map<String, Object> comparableMap = new LinkedHashMap<>();
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    final Entity entity = txn.getEntity(idOfEntity);

                    for (String property : entity.getPropertyNames()) {
                        comparableMap.put(property, entity.getProperty(property));
                    }

                    List<String> aclRead = new LinkedList<>();
                    List<String> aclWrite = new LinkedList<>();

                    Boolean publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    Boolean publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                    for (Entity aclReadLink : entity.getLinks(Constants.ACL_READ)) {
                        aclRead.add(aclReadLink.getId().toString());
                    }

                    for (Entity aclWriteLink : entity.getLinks(Constants.ACL_WRITE)) {
                        aclWrite.add(aclWriteLink.getId().toString());
                    }

                    Map<String, Object> metadata = new TreeMap<String, Object>();

                    metadata.put(Constants.ENTITY_ID, idOfEntity.toString());
                    metadata.put(Constants.ACL_READ, aclRead);
                    metadata.put(Constants.ACL_WRITE, aclWrite);
                    metadata.put(Constants.BLOBNAMES, entity.getBlobNames());
                    metadata.put(Constants.LINKS, entity.getLinkNames());

                    comparableMap.put(Constants.METADATA_KEY, metadata);

                }
            });
        } finally {
            entityStore.close();
        }
        return comparableMap;
    }

    @Override
    public List<Map<String, Object>> listEntities(String instance, final String storeName,
                                                  final int skip, final int limit) {
        final List<Map<String, Object>> entities = new LinkedList<>();
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final EntityIterable allUsers = txn.getAll(storeName).skip(skip).take(limit);
                    for (Entity entity : allUsers) {
                        final Map<String, Object> comparableMap = new LinkedHashMap<>();
                        for (String property : entity.getPropertyNames()) {
                            comparableMap.put(property, entity.getProperty(property));
                        }

                        List<String> aclRead = new LinkedList<>();
                        List<String> aclWrite = new LinkedList<>();

                        Boolean publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                        Boolean publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                        for (Entity aclReadLink : entity.getLinks(Constants.ACL_READ)) {
                            aclRead.add(aclReadLink.getId().toString());
                        }

                        for (Entity aclWriteLink : entity.getLinks(Constants.ACL_WRITE)) {
                            aclWrite.add(aclWriteLink.getId().toString());
                        }

                        Map<String, Object> metadata = new TreeMap<String, Object>();

                        metadata.put(Constants.ENTITY_ID, entity.getId().toString());
                        metadata.put(Constants.ACL_READ, aclRead);
                        metadata.put(Constants.ACL_WRITE, aclWrite);
                        metadata.put(Constants.BLOBNAMES, entity.getBlobNames());
                        metadata.put(Constants.LINKS, entity.getLinkNames());

                        comparableMap.put(Constants.METADATA_KEY, metadata);
                        entities.add(comparableMap);
                    }
                }
            });
        } finally {
            entityStore.close();
        }
        return entities;
    }

    @Override
    public Comparable getEntityProperty(String instance, final String storeName, final String entityId,
                                        final String propertyName) {
        final Comparable[] comparable = new Comparable[1];
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    final Entity entity = txn.getEntity(idOfEntity);
                    comparable[0] = entity.getProperty(propertyName);
                }
            });
        } finally {
            entityStore.close();
        }
        return comparable[0];
    }

    @Override
    public InputStream getEntityBlob(String instance, String storeName, final String entityId, final String blobKey) {
        final InputStream[] inputStream = new InputStream[1];
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    final Entity entity = txn.getEntity(idOfEntity);
                    inputStream[0] = entity.getBlob(blobKey);
                }
            });
        } finally {
            entityStore.close();
        }
        return inputStream[0];
    }

    @Override
    public boolean deleteEntity(String instance, String storeName, final String entityId) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId roleEntityId = txn.toEntityId(entityId);
                    Entity entity = txn.getEntity(roleEntityId);
                    success[0] = entity.delete();
                }
            });
        } finally {
            entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean linkEntity(String instance, String storeName, final String linkName, final String sourceId, final String targetId) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId userEntityId = txn.toEntityId(sourceId);
                    EntityId roleEntityId = txn.toEntityId(targetId);
                    Entity sourceEntity = txn.getEntity(userEntityId);
                    Entity targetEntity = txn.getEntity(roleEntityId);
                    success[0] = sourceEntity.addLink(linkName, targetEntity);
                }
            });
        } finally {
            entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean unlinkEntity(String instance, String storeName, final String linkName, final String entityId, final String targetId) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId sourceEntityId = txn.toEntityId(entityId);
                    EntityId targetEntityId = txn.toEntityId(targetId);
                    Entity sourceEntity = txn.getEntity(sourceEntityId);
                    Entity targetEntity = txn.getEntity(targetEntityId);
                    success[0] = sourceEntity.deleteLink(linkName, targetEntity);
                }
            });
        } finally {
            entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean isLinked(String instance, String storeName, final String linkName, final String entityId, final String targetId) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId sourceEntityId = txn.toEntityId(entityId);
                    EntityId targetEntityId = txn.toEntityId(targetId);
                    Entity sourceEntity = txn.getEntity(sourceEntityId);
                    Entity targetEntity = txn.getEntity(targetEntityId);
                    Entity linkedRole = sourceEntity.getLink(linkName);
                    success[0] = linkedRole.getId().toString().equals(targetEntity.getId().toString());
                }
            });
        } finally {
            entityStore.close();
        }
        return success[0];
    }

    @Override
    public Map<String, Object> getFirstLinkedEntity(String instance, String storeName, final String entityId, final String linkName) {
        final Map<String, Object> comparableMap = new LinkedHashMap<>();
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId sourceEntityId = txn.toEntityId(entityId);
                    final Entity source = txn.getEntity(sourceEntityId);

                    Entity entity = source.getLink(linkName);

                    for (String property : entity.getPropertyNames()) {
                        comparableMap.put(property, entity.getProperty(property));
                    }

                    List<String> aclRead = new LinkedList<>();
                    List<String> aclWrite = new LinkedList<>();

                    Boolean publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    Boolean publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                    for (Entity aclReadLink : entity.getLinks(Constants.ACL_READ)) {
                        aclRead.add(aclReadLink.getId().toString());
                    }

                    for (Entity aclWriteLink : entity.getLinks(Constants.ACL_WRITE)) {
                        aclWrite.add(aclWriteLink.getId().toString());
                    }

                    Map<String, Object> metadata = new TreeMap<String, Object>();

                    metadata.put(Constants.ENTITY_ID, entity.getId().toString());
                    metadata.put(Constants.ACL_READ, aclRead);
                    metadata.put(Constants.ACL_WRITE, aclWrite);
                    metadata.put(Constants.BLOBNAMES, entity.getBlobNames());
                    metadata.put(Constants.LINKS, entity.getLinkNames());

                    comparableMap.put(Constants.METADATA_KEY, metadata);
                }
            });
        } finally {
            entityStore.close();
        }
        return comparableMap;
    }

    @Override
    public List<Map<String, Object>> getLinkedEntities(String instance, String storeName, String entityId, String linkName) {
        return null;
    }
}
