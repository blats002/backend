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
import com.divroll.domino.model.UndefinedBinding;
import com.divroll.domino.model.UndefinedIterable;
import com.divroll.domino.repository.EntityRepository;
import com.divroll.domino.repository.RoleRepository;
import com.divroll.domino.xodus.XodusManager;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.*;
import org.jetbrains.annotations.NotNull;
import scala.actors.threadpool.Arrays;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeEntityRepository implements EntityRepository {

    private static final Logger LOG
            = Logger.getLogger(JeeEntityRepository.class.getName());

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    @Named("defaultRoleStore")
    String defaultRoleStore;

    @Inject
    RoleRepository roleRepository;

    @Inject
    XodusManager manager;

    @Override
    public String createEntity(final String instance, final String storeName, final Map<String, Comparable> comparableMap,
                               final String[] read, final String[] write, final Boolean publicRead, final Boolean publicWrite) {

        final String[] entityId = {null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {

                    try {
                        entityStore.registerCustomPropertyType(txn, UndefinedIterable.class, UndefinedBinding.BINDING);
                    } catch (Exception e) {

                    }

                    final Entity entity = txn.newEntity(storeName);
                    Iterator<String> it = comparableMap.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        Comparable value = comparableMap.get(key);
                        if (value == null) {
                            if (!key.equals(Constants.RESERVED_FIELD_PUBLICREAD)
                                    && !key.equals(Constants.RESERVED_FIELD_PUBLICWRITE)) {
                                entity.setProperty(key, new UndefinedIterable());
                            }
                        } else {
                            if (!key.equals(Constants.RESERVED_FIELD_PUBLICREAD)
                                    && !key.equals(Constants.RESERVED_FIELD_PUBLICWRITE)) {
                                // TODO: If value is JSONObject create a new Entity and link it to this entity
                                entity.setProperty(key, value);
                            }
                        }
                    }

                    if (read != null) {
                        List<String> aclRead = Arrays.asList(read);
                        // Add User to ACL
                        for (String userOrRoleId : aclRead) {
                            if (userOrRoleId != null && !userOrRoleId.isEmpty()) {
                                EntityId userorRoleEntityId = txn.toEntityId(userOrRoleId);
                                Entity userOrRoleEntity = txn.getEntity(userorRoleEntityId);
                                if (userOrRoleEntity != null) {
                                    entity.addLink(Constants.ACL_READ, userOrRoleEntity);
                                    entity.setProperty("read(" + userOrRoleEntity.getId().toString() + ")", true);
                                }
                            }

                        }
                    }

                    if (write != null) {
                        List<String> aclWrite = Arrays.asList(write);
                        // Add User to ACL
                        for (String userId : aclWrite) {
                            if (userId != null && !userId.isEmpty()) {
                                EntityId userEntityId = txn.toEntityId(userId);
                                Entity userOrRoleEntity = txn.getEntity(userEntityId);
                                if (userOrRoleEntity != null) {
                                    entity.addLink(Constants.ACL_WRITE, userOrRoleEntity);
                                    entity.setProperty("write(" + userOrRoleEntity.getId().toString() + ")", true);
                                }
                            }
                        }
                    }

                    if (publicRead != null) {
                        entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                    } else {
                        entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, new UndefinedIterable());
                    }

                    if (publicWrite != null) {
                        entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
                    } else {
                        entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, new UndefinedIterable());
                    }


                    entityId[0] = entity.getId().toString();
                }
            });
        } finally {
            ////entityStore.close();
        }
        return entityId[0];
    }

    @Override
    public boolean updateEntity(String instance, String storeName, final String entityId, final Map<String, Comparable> comparableMap,
                                final String[] read, final String[] write, final Boolean publicRead, final Boolean publicWrite) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
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
                        if (value == null) {
                            entity.setProperty(key, new UndefinedIterable());
                        } else {
                            entity.setProperty(key, value);
                        }
                    }

                    if (read != null) {
                        boolean publicRead = true;
                        List<String> aclRead = Arrays.asList(read);
                        // Add User to ACL
                        for (String userId : aclRead) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userOrRoleEntity = txn.getEntity(userEntityId);
                            if (userOrRoleEntity != null) {
                                entity.addLink(Constants.ACL_READ, userOrRoleEntity);
                                entity.setProperty("read(" + userOrRoleEntity.getId().toString() + ")", true);
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
                            Entity userOrRoleEntity = txn.getEntity(userEntityId);
                            if (userOrRoleEntity != null) {
                                entity.addLink(Constants.ACL_WRITE, userOrRoleEntity);
                                entity.setProperty("write(" + userOrRoleEntity.getId().toString() + ")", true);
                            }
                        }
                        entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
                    }

                    success[0] = true;
                }
            });
        } finally {
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public Map<String, Object> getEntity(String instance, final String storeName, final String entityId) {
        final Map<String, Object> comparableMap = new LinkedHashMap<>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    final Entity entity = txn.getEntity(idOfEntity);

                    for (String property : entity.getPropertyNames()) {
                        Comparable value = entity.getProperty(property);
                        if (value instanceof UndefinedIterable) {
                            comparableMap.put(property, null);
                        } else {
                            comparableMap.put(property, value);
                        }
                    }

                    List<String> aclRead = new LinkedList<>();
                    List<String> aclWrite = new LinkedList<>();

                    Comparable comparablePublicRead = entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    Comparable comparablePublicWrite = entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                    Boolean publicRead = null;
                    Boolean publicWrite = null;

                    if (comparablePublicRead instanceof UndefinedIterable) {
                        publicRead = null;
                    } else {
                        publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    }

                    if (comparablePublicWrite instanceof UndefinedIterable) {
                        publicWrite = null;
                    } else {
                        publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);
                    }

                    for (Entity aclReadLink : entity.getLinks(Constants.ACL_READ)) {
                        aclRead.add(aclReadLink.getId().toString());
                    }

                    for (Entity aclWriteLink : entity.getLinks(Constants.ACL_WRITE)) {
                        aclWrite.add(aclWriteLink.getId().toString());
                    }
                    comparableMap.put(Constants.ENTITY_ID, idOfEntity.toString());
                    comparableMap.put(Constants.ACL_READ, aclRead);
                    comparableMap.put(Constants.ACL_WRITE, aclWrite);
                    comparableMap.put(Constants.BLOBNAMES, entity.getBlobNames());
                    comparableMap.put(Constants.LINKS, entity.getLinkNames());
                    comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
                    comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                }
            });
        } finally {
            ////entityStore.close();
        }
        return comparableMap;
    }

    @Override
    public Comparable getEntityProperty(String instance, final String storeName, final String entityId,
                                        final String propertyName) {
        final Comparable[] comparable = new Comparable[1];
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    final Entity entity = txn.getEntity(idOfEntity);
                    Comparable value = entity.getProperty(propertyName);
                    if (value instanceof UndefinedIterable) {
                        comparable[0] = null;
                    } else {
                        comparable[0] = entity.getProperty(propertyName);
                    }
                }
            });
        } finally {
            ////entityStore.close();
        }
        return comparable[0];
    }

    @Override
    public InputStream getEntityBlob(String instance, String storeName, final String entityId, final String blobKey) {
        final InputStream[] inputStream = new InputStream[1];
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
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
            ////entityStore.close();
        }
        return inputStream[0];
    }

    @Override
    public boolean createEntityBlob(String instance, String storeName, String entityId, String blobKey, InputStream is) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    final Entity entity = txn.getEntity(idOfEntity);
                    entity.setBlob(blobKey, is);
                    success[0] = true;
                }
            });
        } finally {
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean deleteEntityBlob(String instance, String storeName, String entityId, String blobKey) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    final Entity entity = txn.getEntity(idOfEntity);
                    entity.deleteBlob(blobKey);
                    success[0] = true;
                }
            });
        } finally {
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public List<String> getLinkNames(String instance, String storeName, String entityId) {
        final List<String>[] result = new List[]{new LinkedList<>()};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    final Entity entity = txn.getEntity(idOfEntity);
                    result[0] = entity.getLinkNames();
                }
            });
        } finally {
            ////entityStore.close();
        }
        return result[0];
    }

    @Override
    public List<String> getBlobKeys(String instance, String storeName, String entityId) {
        final List<String>[] result = new List[]{new LinkedList<>()};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    final Entity entity = txn.getEntity(idOfEntity);
                    result[0] = entity.getBlobNames();
                }
            });
        } finally {
            ////entityStore.close();
        }
        return result[0];
    }

    @Override
    public boolean deleteEntity(String instance, String storeName, final String entityId) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
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
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean linkEntity(String instance, String storeName, final String linkName, final String sourceId, final String targetId) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
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
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean unlinkEntity(String instance, String storeName, final String linkName, final String entityId, final String targetId) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
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
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean isLinked(String instance, String storeName, final String linkName, final String entityId, final String targetId) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
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
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public Map<String, Object> getFirstLinkedEntity(String instance, String storeName, final String entityId, final String linkName) {
        final Map<String, Object> comparableMap = new LinkedHashMap<>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId sourceEntityId = txn.toEntityId(entityId);
                    final Entity source = txn.getEntity(sourceEntityId);

                    Entity entity = source.getLink(linkName);

                    for (String property : entity.getPropertyNames()) {
                        Comparable value = entity.getProperty(property);
                        if (value instanceof UndefinedIterable) {
                            comparableMap.put(property, null);
                        } else {
                            comparableMap.put(property, value);
                        }
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

                    comparableMap.put(Constants.ENTITY_ID, entity.getId().toString());
                    comparableMap.put(Constants.ACL_READ, aclRead);
                    comparableMap.put(Constants.ACL_WRITE, aclWrite);
                    comparableMap.put(Constants.BLOBNAMES, entity.getBlobNames());
                    comparableMap.put(Constants.LINKS, entity.getLinkNames());
                    comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                    comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);

                }
            });
        } finally {
            ////entityStore.close();
        }
        return comparableMap;
    }

    @Override
    public List<Map<String, Object>> getLinkedEntities(String instance, String storeName, String entityId, String linkName) {
        return null;
    }

    @Override
    public List<Map<String, Object>> listEntities(String instance, String storeName, String userIdRoleId,
                                                  int skip, int limit, String sort, boolean isMasterKey) {
        final List<Map<String, Object>> entities = new LinkedList<Map<String, Object>>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityIterable result = null;
                    if (isMasterKey) {
                        result = txn.getAll(storeName).skip(skip).take(limit);
                    } else if (userIdRoleId == null) {
                        result = txn.find(storeName, "publicRead", true);
                        long count = result.count();
                        LOG.info("COUNT: " + count);
                        if (sort != null) {
                            if (sort.startsWith("-")) {
                                String sortDescending = sort.substring(1);
                                result = txn.sort(storeName, sortDescending, result, false);
                            } else {
                                String sortAscending = sort.substring(1);
                                result = txn.sort(storeName, sortAscending, result, true);
                            }
                        }

                    } else {
                        result = txn.find(storeName, "read(" + userIdRoleId + ")", true)
                                .concat(txn.find(storeName, "publicRead", true));
                        if (sort != null) {
                            if (sort.startsWith("-")) {
                                String sortDescending = sort.substring(1);
                                result = txn.sort(storeName, sortDescending, result, false);
                            } else {
                                String sortAscending = sort.substring(1);
                                result = txn.sort(storeName, sortAscending, result, true);
                            }
                        }
                    }
                    result = result.skip(skip).take(limit);
                    for (Entity entity : result) {
                        final Map<String, Object> comparableMap = new LinkedHashMap<>();
                        for (String property : entity.getPropertyNames()) {
                            Comparable value = entity.getProperty(property);
                            if (value instanceof UndefinedIterable) {
                                comparableMap.put(property, null);
                            } else {
                                comparableMap.put(property, value);
                            }
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

                        comparableMap.put(Constants.ENTITY_ID, entity.getId().toString());
                        comparableMap.put(Constants.ACL_READ, aclRead);
                        comparableMap.put(Constants.ACL_WRITE, aclWrite);
                        comparableMap.put(Constants.BLOBNAMES, entity.getBlobNames());
                        comparableMap.put(Constants.LINKS, entity.getLinkNames());
                        comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                        comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);

                        entities.add(comparableMap);
                    }
                }
            });
        } finally {
            ////entityStore.close();
        }
        return entities;
    }
}
