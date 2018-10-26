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
import com.divroll.backend.model.*;
import com.divroll.backend.model.action.Action;
import com.divroll.backend.model.builder.EntityClass;
import com.divroll.backend.model.builder.EntityClassBuilder;
import com.divroll.backend.model.filter.TransactionFilter;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.xodus.XodusManager;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
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
public class JeeEntityRepository extends JeeBaseRespository implements EntityRepository {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeEntityRepository.class);

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    @Named("defaultRoleStore")
    String defaultRoleStore;

    @Inject
    @Named("defaultUserStore")
    String defaultUserStore;

    @Inject
    XodusManager manager;

    @Override
    public String createEntity(final String instance, final String storeName, EntityClass entityClass, List<Action> actions) {
        final String[] entityId = {null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final Entity entity = txn.newEntity(storeName);
                    Iterator<String> it = entityClass.comparableMap().keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        Comparable value = entityClass.comparableMap().get(key);
                        if (value == null) {
                            if (!key.equals(Constants.RESERVED_FIELD_PUBLICREAD)
                                    && !key.equals(Constants.RESERVED_FIELD_PUBLICWRITE)
                                    && !key.equals(Constants.RESERVED_FIELD_ACL_WRITE)
                                    && !key.equals(Constants.RESERVED_FIELD_ACL_READ)) {
                               entity.deleteProperty(key);
                            }
                        } else {
                            if (!key.equals(Constants.RESERVED_FIELD_PUBLICREAD)
                                    && !key.equals(Constants.RESERVED_FIELD_PUBLICWRITE)
                                    && !key.equals(Constants.RESERVED_FIELD_ACL_WRITE)
                                    && !key.equals(Constants.RESERVED_FIELD_ACL_READ)
                                    && !key.equals(Constants.RESERVED_FIELD_BLOBNAMES)
                                    && !key.equals(Constants.RESERVED_FIELD_LINKS)) {
//                                if(value instanceof EmbeddedEntityIterable) {
//                                    LOG.info(value.toString());
//                                }
                                entity.setProperty(key, value);
                            }
                        }
                    }

                    if (entityClass.read() != null) {
                        List<String> aclRead = Arrays.asList(entityClass.read());
                        // Add User to ACL
                        for (String userOrRoleId : aclRead) {
                            if (userOrRoleId != null && !userOrRoleId.isEmpty()) {
                                EntityId userorRoleEntityId = txn.toEntityId(userOrRoleId);
                                Entity userOrRoleEntity = txn.getEntity(userorRoleEntityId);
                                if (userOrRoleEntity != null) {
                                    entity.addLink(Constants.RESERVED_FIELD_ACL_READ, userOrRoleEntity);
                                }
                            }

                        }
                    }

                    if (entityClass.write() != null) {
                        List<String> aclWrite = Arrays.asList(entityClass.write());
                        // Add User to ACL
                        for (String userId : aclWrite) {
                            if (userId != null && !userId.isEmpty()) {
                                EntityId userEntityId = txn.toEntityId(userId);
                                Entity userOrRoleEntity = txn.getEntity(userEntityId);
                                if (userOrRoleEntity != null) {
                                    entity.addLink(Constants.RESERVED_FIELD_ACL_WRITE, userOrRoleEntity);
                                }
                            }
                        }
                    }

                    if (entityClass.publicRead() != null) {
                        entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, entityClass.publicRead());
                    } else {
                        entity.deleteProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    }

                    if (entityClass.publicWrite() != null) {
                        entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, entityClass.publicWrite());
                    } else {
                        entity.deleteProperty(Constants.RESERVED_FIELD_PUBLICWRITE);
                    }

                    entityId[0] = entity.getId().toString();

                    Map<String,Comparable> eMap = new LinkedHashMap<>(entityClass.comparableMap());
                    eMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId[0]);
                    EntityClass created = new EntityClassBuilder().from(entityClass).comparableMap(eMap).build();

                    if(actions != null) {
                        actions.forEach(action -> {
                            if(action.actionOp().equals(Action.ACTION_OP.LINK)) {
                                String entityType = action.entityType().get();
                                Map<String,Comparable> entityMap = action.entity().get();
                                String linkName = action.linkName().get();
                                String backLinkName = action.backLinkName().get();

                                final Entity linkedEntity = txn.newEntity(entityType);
                                entityMap.forEach((key,value) -> {
                                    linkedEntity.setProperty(key, value);
                                });
                                entity.addLink(linkName, linkedEntity);
                                if(backLinkName != null && !backLinkName.isEmpty()) {
                                    linkedEntity.addLink(backLinkName, entity);
                                }

                                Action next = action.next().get();
                                if(next != null) {
                                    if(next.actionOp().equals(Action.ACTION_OP.SET)) {
                                        String propName = next.propertyName().get();
                                        String refPropName = next.referenceProperty().get();
                                        Comparable refPropValue = created.comparableMap().get(refPropName);
                                        if(propName.equals(Constants.RESERVED_FIELD_ACL_READ)) {
                                            EntityId referencedEntity = txn.toEntityId((String) refPropValue);
                                            linkedEntity.addLink(Constants.RESERVED_FIELD_ACL_READ, txn.getEntity(referencedEntity));
                                        } else if(propName.equals(Constants.RESERVED_FIELD_ACL_WRITE)) {
                                            EntityId referencedEntity = txn.toEntityId((String) refPropValue);
                                            linkedEntity.addLink(Constants.RESERVED_FIELD_ACL_WRITE, txn.getEntity(referencedEntity));
                                        }

                                    }
                                }

                            }
                        });
                    }


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
                    EntityId idOfEntity = txn.toEntityId(entityId);

                    final Entity entity = txn.getEntity(idOfEntity);
                    Iterator<String> it = comparableMap.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        Comparable value = comparableMap.get(key);
                        if (value == null) {
                            entity.deleteProperty(key);
                        } else {
                            entity.setProperty(key, value);
                        }
                    }

                    if (read != null) {
                        List<String> aclRead = Arrays.asList(read);
                        // Add User to ACL
                        entity.deleteLinks(Constants.RESERVED_FIELD_ACL_READ);
                        for (String userId : aclRead) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userOrRoleEntity = txn.getEntity(userEntityId);
                            if (userOrRoleEntity != null) {
                                entity.addLink(Constants.RESERVED_FIELD_ACL_READ, userOrRoleEntity);
                            }
                        }
                        entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                    }

                    if (write != null) {
                        List<String> aclWrite = Arrays.asList(write);
                        // Add User to ACL
                        entity.deleteLinks(Constants.RESERVED_FIELD_ACL_WRITE);
                        for (String userId : aclWrite) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userOrRoleEntity = txn.getEntity(userEntityId);
                            if (userOrRoleEntity != null) {
                                entity.addLink(Constants.RESERVED_FIELD_ACL_WRITE, userOrRoleEntity);
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
    public <T> Map<String, Object> getFirstEntity(String dir, String kind, String propertyKey, Comparable<T> propertyVal, Class<T> clazz) {
        final Map<String, Object> comparableMap = new LinkedHashMap<>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    Entity entity = txn.find(kind, propertyKey, propertyVal).getFirst();
                    for (String property : entity.getPropertyNames()) {
                        Comparable value = entity.getProperty(property);
                        if(value != null) {
                            if(value instanceof EmbeddedEntityIterable) {
                                comparableMap.put(property, ((EmbeddedEntityIterable) value).asJSONObject());
                            } else if(value instanceof EmbeddedArrayIterable) {
                                comparableMap.put(property, ((EmbeddedArrayIterable) value).asJSONArray());
                            } else {
                                comparableMap.put(property, value);
                            }
                        }
                    }

                    List<EntityStub> aclRead = new LinkedList<>();
                    List<EntityStub> aclWrite = new LinkedList<>();

                    Comparable comparablePublicRead = entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    Comparable comparablePublicWrite = entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                    Boolean publicRead = null;
                    Boolean publicWrite = null;

                    if(comparablePublicRead != null) {
                        publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    }

                    if (comparablePublicWrite != null) {
                        publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);
                    }

                    for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                        aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                    }

                    for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                        aclWrite.add(new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                    }
                    comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.getId().toString());
                    comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, aclRead);
                    comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, aclWrite);
                    comparableMap.put(Constants.RESERVED_FIELD_BLOBNAMES, entity.getBlobNames());
                    comparableMap.put(Constants.RESERVED_FIELD_LINKS, entity.getLinkNames());
                    comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
                    comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                }
            });
        } finally {
            //entityStore.close();
        }
        return comparableMap;
    }

    @Override
    public <T> InputStream getFirstEntityBlob(String appId, String kind, String propertyKey, Comparable<T> propertyVal, Class<T> clazz, String blobKey) {
        System.out.println("appId = " + appId);
        final InputStream[] inputStream = new InputStream[1];
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appId);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    Entity entity = txn.find(kind, propertyKey, propertyVal).getFirst();
                    if(entity != null) {
                        inputStream[0] = entity.getBlob(blobKey);
                    }
                }
            });
        } finally {
            ////entityStore.close();
        }
        return inputStream[0];
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
                        if(value != null) {
                            if(value instanceof EmbeddedEntityIterable) {
                                comparableMap.put(property, ((EmbeddedEntityIterable) value).asJSONObject());
                            } else if(value instanceof EmbeddedArrayIterable) {
                                comparableMap.put(property, ((EmbeddedArrayIterable) value).asJSONArray());
                            } else {
                                comparableMap.put(property, value);
                            }
                        }
                    }

                    List<EntityStub> aclRead = new LinkedList<>();
                    List<EntityStub> aclWrite = new LinkedList<>();

                    Comparable comparablePublicRead = entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    Comparable comparablePublicWrite = entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                    Boolean publicRead = null;
                    Boolean publicWrite = null;

                    if(comparablePublicRead != null) {
                        publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    }

                    if (comparablePublicWrite != null) {
                        publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);
                    }

                    for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                        aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                    }

                    for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                        aclWrite.add(new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                    }
                    comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, idOfEntity.toString());
                    comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, aclRead);
                    comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, aclWrite);
                    comparableMap.put(Constants.RESERVED_FIELD_BLOBNAMES, entity.getBlobNames());
                    comparableMap.put(Constants.RESERVED_FIELD_LINKS, entity.getLinkNames());
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
                    comparable[0] = entity.getProperty(propertyName);
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
    public boolean deleteProperty(String instance, String storeName, String propertyName) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityIterable entities = txn.findWithProp(storeName, propertyName);
                    final boolean[] hasError = {false};
                    entities.forEach(entity -> {
                        if(!entity.deleteProperty(propertyName)) {
                            hasError[0] = true;
                        }
                    });
                    success[0] = !hasError[0];
                }
            });
        } finally {
            //entityStore.close();
        }
        return success[0];
    }

    @Override
    public List<Map<String, Object>> getEntities(String instance, String storeName, String propertyName, Comparable propertyValue, int skip, int limit) {
        final List<Map<String, Object>> entities = new LinkedList<Map<String, Object>>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityIterable result = txn.find(storeName, propertyName, propertyValue).skip(skip).take(limit);
                    result = result.skip(skip).take(limit);
                    for (Entity entity : result) {
                        final Map<String, Object> comparableMap = new LinkedHashMap<>();
                        for (String property : entity.getPropertyNames()) {
                            Comparable value = entity.getProperty(property);
                            if(value != null) {
                                if(value != null) {
                                    if(value instanceof EmbeddedEntityIterable) {
                                        comparableMap.put(property, ((EmbeddedEntityIterable) value).asJSONObject());
                                    } else if(value instanceof EmbeddedArrayIterable) {
                                        comparableMap.put(property, ((EmbeddedArrayIterable) value).asJSONArray());
                                    } else {
                                        comparableMap.put(property, value);
                                    }
                                }                            }
                        }

                        List<EntityStub> aclRead = new LinkedList<>();
                        List<EntityStub> aclWrite = new LinkedList<>();

                        Boolean publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                        Boolean publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                        for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                            aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                        }

                        for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                            aclWrite.add(new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                        }

                        comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.getId().toString());
                        comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, aclRead);
                        comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, aclWrite);
                        comparableMap.put(Constants.RESERVED_FIELD_BLOBNAMES, entity.getBlobNames());
                        comparableMap.put(Constants.RESERVED_FIELD_LINKS, entity.getLinkNames());
                        comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                        comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
                        if(entity.getType().equals(defaultUserStore)) {
                            comparableMap.remove(Constants.RESERVED_FIELD_PASSWORD);
                        }
                        entities.add(comparableMap);
                    }
                }
            });
        } finally {
            ////entityStore.close();
        }
        return entities;
    }

    @Override
    public boolean deleteEntity(String instance, String storeName, final String entityId) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    Entity entity = txn.getEntity(idOfEntity);
                    success[0] = entity.delete();
                }
            });
        } finally {
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean deleteEntities(String instance, final String storeName) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityIterable result = txn.getAll(storeName);
                    final boolean[] hasError = {false};
                    for(Entity entity : result) {
                        if(!entity.delete()) {
                            hasError[0] = true;
                        }
                    }
                    success[0] = !hasError[0];
                }
            });
        } finally {
            ////entityStore.close();
        }

        return success[0];
    }

    @Override
    public boolean deleteEntityType(String instance, String entityType) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            PersistentEntityStoreImpl storeImp = (PersistentEntityStoreImpl) entityStore;
            storeImp.deleteEntityType(entityType);
            success[0] = true;
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
                    EntityId idOfSource = txn.toEntityId(sourceId);
                    EntityId idOfTarget = txn.toEntityId(targetId);
                    Entity sourceEntity = txn.getEntity(idOfSource);
                    Entity targetEntity = txn.getEntity(idOfTarget);
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
                        if(value != null) {
                            comparableMap.put(property, value);
                        }
                    }

                    List<EntityStub> aclRead = new LinkedList<>();
                    List<EntityStub> aclWrite = new LinkedList<>();

                    Boolean publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    Boolean publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                    for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                        aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                    }

                    for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                        aclWrite.add(new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                    }

                    comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.getId().toString());
                    comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, aclRead);
                    comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, aclWrite);
                    comparableMap.put(Constants.RESERVED_FIELD_BLOBNAMES, entity.getBlobNames());
                    comparableMap.put(Constants.RESERVED_FIELD_LINKS, entity.getLinkNames());
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
    public List<Map<String, Object>> getLinkedEntities(String instance, String storeName,
                                                       final String entityId, final String linkName) {
        final List<Map<String, Object>> entities = new LinkedList<Map<String, Object>>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    Entity txnEntity = txn.getEntity(idOfEntity);
                    EntityIterable result = txnEntity.getLinks(Arrays.asList(new String[]{linkName}));
                    for (Entity entity : result) {
                        final Map<String, Object> comparableMap = new LinkedHashMap<>();
                        for (String property : entity.getPropertyNames()) {
                            Comparable value = entity.getProperty(property);
                            if(value != null) {
                                if(value != null) {
                                    if(value instanceof EmbeddedEntityIterable) {
                                        comparableMap.put(property, ((EmbeddedEntityIterable) value).asJSONObject());
                                    } else if(value instanceof EmbeddedArrayIterable) {
                                        comparableMap.put(property, ((EmbeddedArrayIterable) value).asJSONArray());
                                    } else {
                                        comparableMap.put(property, value);
                                    }
                                }                            }
                        }

                        List<EntityStub> aclRead = new LinkedList<>();
                        List<EntityStub> aclWrite = new LinkedList<>();

                        Boolean publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                        Boolean publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                        for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                            aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                        }

                        for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                            aclWrite.add(new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                        }


                        comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.getId().toString());
                        comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, aclRead);
                        comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, aclWrite);
                        comparableMap.put(Constants.RESERVED_FIELD_BLOBNAMES, entity.getBlobNames());
                        comparableMap.put(Constants.RESERVED_FIELD_LINKS, entity.getLinkNames());
                        comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                        comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);

                        if(entity.getType().equals(defaultUserStore)) {
                            comparableMap.remove(Constants.RESERVED_FIELD_PASSWORD);
                        }
                        comparableMap.put("entityType", entity.getType());
                        entities.add(comparableMap);
                    }
                }
            });
        } finally {

        }
        return entities;
    }

    @Override
    public List<Map<String, Object>> listEntities(String instance, String storeName, String userIdRoleId,
                                                  int skip, int limit, String sort, boolean isMasterKey, List<TransactionFilter> filters) {
        final List<Map<String, Object>> entities = new LinkedList<Map<String, Object>>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityIterable result = null;
                    if (isMasterKey) {
                        result = txn.getAll(storeName).skip(skip).take(limit);
                        if(filters != null && !filters.isEmpty()) {
                            result = filter(storeName, result, filters, txn);
                        }
                    } else if (userIdRoleId == null) {
                        result = txn.find(storeName, "publicRead", true);
                        long count = result.count();
                        LOG.info("COUNT: " + count);
                        if(filters != null && !filters.isEmpty()) {
                            result = filter(storeName, result, filters, txn);
                        }
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
                        Entity targetEntity = txn.getEntity(txn.toEntityId(userIdRoleId));
                        result = txn.findLinks(storeName, targetEntity, "aclRead")
                                .concat(txn.find(storeName, "publicRead", true));
                        if(filters != null && !filters.isEmpty()) {
                            result = filter(storeName, result, filters, txn);
                        }
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
                            if(value != null) {
                                if(value != null) {
                                    if(value instanceof EmbeddedEntityIterable) {
                                        comparableMap.put(property, ((EmbeddedEntityIterable) value).asJSONObject());
                                    } else if(value instanceof EmbeddedArrayIterable) {
                                        comparableMap.put(property, ((EmbeddedArrayIterable) value).asJSONArray());
                                    } else {
                                        comparableMap.put(property, value);
                                    }
                                }                            }
                        }

                        List<EntityStub> aclRead = new LinkedList<>();
                        List<EntityStub> aclWrite = new LinkedList<>();

                        Boolean publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                        Boolean publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                        for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                            aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                        }

                        for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                            aclWrite.add(new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                        }

                        comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.getId().toString());
                        comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, aclRead);
                        comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, aclWrite);
                        comparableMap.put(Constants.RESERVED_FIELD_BLOBNAMES, entity.getBlobNames());
                        comparableMap.put(Constants.RESERVED_FIELD_LINKS, entity.getLinkNames());
                        comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                        comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
                        if(entity.getType().equals(defaultUserStore)) {
                            comparableMap.remove(Constants.RESERVED_FIELD_PASSWORD);
                        }
                        entities.add(comparableMap);
                    }
                }
            });
        } finally {
            ////entityStore.close();
        }
        return entities;
    }

    @Override
    protected String getRoleStoreName() {
        return defaultRoleStore;
    }
}
