/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019, Divroll, and individual contributors
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
import com.divroll.backend.helper.Comparables;
import com.divroll.backend.model.*;
import com.divroll.backend.model.action.Action;
import com.divroll.backend.model.action.BacklinkAction;
import com.divroll.backend.model.action.EntityAction;
import com.divroll.backend.model.action.LinkAction;
import com.divroll.backend.model.builder.CreateOption;
import com.divroll.backend.model.builder.EntityClass;
import com.divroll.backend.model.builder.EntityClassBuilder;
import com.divroll.backend.model.builder.EntityMetadata;
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
import util.ComparableHashMap;
import util.ComparableLinkedList;

import java.io.InputStream;
import java.util.*;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeEntityRepository extends JeeBaseRespository implements EntityRepository {

  private static final Logger LOG = LoggerFactory.getLogger(JeeEntityRepository.class);

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
  @Named("namespaceProperty")
  String namespaceProperty;

  @Inject
  @Named("metadataProperty")
  String metadataProperty;

  @Inject XodusManager manager;

  @Override
  public String createEntity(
      final String instance,
      String namespace,
      final String entityType,
      EntityClass entityClass,
      List<Action> actions,
      List<EntityAction> entityActions,
      CreateOption createOption,
      final EntityMetadata metadata) {
    final String[] entityId = {null};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {

              Entity first = null;

              Entity entityToUpdate = null;

              if(namespace != null) {
                  first = txn.find(entityType, namespaceProperty, namespace).getFirst();
              } else {
                  first = txn.getAll(entityType).minus(txn.findWithProp(entityType, namespaceProperty)).getFirst();
              }

              if(createOption != null) {
                CreateOption.CREATE_OPTION createOp = createOption.createOption();
                if(createOp != null && createOp.equals(CreateOption.CREATE_OPTION.SET_BLOB_ON_PROPERTY_EQUALS)) {
                  String propertyName = createOption.referencePropertyName();
                  Comparable propertyValue = entityClass.comparableMap().get(propertyName);
                  if(namespace != null) {
                    EntityIterable result = txn.find(entityType, propertyName, propertyValue).intersect(txn.find(entityType, namespaceProperty, namespace));
                    if(result.size() > 1) {
                        throw new IllegalArgumentException("Multiple entities found");
                    }
                    entityToUpdate = result.getFirst();
                  } else {
                    EntityIterable result = txn.find(entityType, propertyName, propertyValue).minus(txn.findWithProp(entityType, namespaceProperty));
                      if(result.size() > 1) {
                          throw new IllegalArgumentException("Multiple entities found");
                      }
                    entityToUpdate = result.getFirst();
                  }
                }
              }

              List<String> uniqueList = new LinkedList<>();
              ComparableHashMap metaDataHashMap = null;
              if(first != null && metadata.uniqueProperties() == null) {
                  EmbeddedEntityIterable embeddedEntityIterable = first.getProperty(metadataProperty) != null
                          ? (EmbeddedEntityIterable) first.getProperty(metadataProperty)
                          : null;
                  if(embeddedEntityIterable != null) {
                      metaDataHashMap = (ComparableHashMap) embeddedEntityIterable.asObject();
                      if(metaDataHashMap != null) {
                          ComparableLinkedList comparableLinkedList = metaDataHashMap.get("uniqueProperties") != null
                                  ? (ComparableLinkedList) metaDataHashMap.get("uniqueProperties")
                                  : null;
                          uniqueList.addAll(comparableLinkedList);
                      }
                  }
              } else {
                  if(metadata.uniqueProperties() != null) {
                      uniqueList.addAll(metadata.uniqueProperties());
                  }
              }

              final EntityIterable[] iterable = new EntityIterable[1];
              if (!uniqueList.isEmpty()) {
                  uniqueList
                    .forEach(
                        property -> {
                          if ( (metadata.uniqueProperties() != null && !metadata.uniqueProperties().isEmpty())
                                  && !entityClass.comparableMap().keySet().contains(property)) {
                            throw new IllegalArgumentException("Invalid unique property name");
                          }

                          Comparable propertyValue = entityClass.comparableMap().get(property);
                          if (iterable[0] == null && propertyValue != null) {
                            iterable[0] = txn.find(entityType, property, propertyValue);
                          }
                          if (propertyValue != null) {
                            iterable[0] =
                                iterable[0].union(txn.find(entityType, property, propertyValue));
                          }
                        });
              }

              if(entityToUpdate != null) {
                  if(iterable[0] != null && !iterable[0].isEmpty()
                          && !iterable[0].getFirst().getId().equals(entityToUpdate.getId())) {
                      throw new IllegalArgumentException("Duplicate entities found " + iterable[0].size());
                  }
              } else {
                  if (iterable[0] != null && !iterable[0].isEmpty()) {
                      throw new IllegalArgumentException("Duplicate entities found " + iterable[0].size());
                  }
              }


              Entity entity = null;
              if(entityToUpdate != null) {
                  entity = entityToUpdate;
              } else {
                  entity = txn.newEntity(entityType);
              }
              Entity finalEntity = entity;

              if (namespace != null && !namespace.isEmpty()) {
                finalEntity.setProperty(namespaceProperty, namespace);
              }

              ComparableLinkedList<Comparable> uProperties = new ComparableLinkedList<>();
              uProperties.addAll(uniqueList);
              removeDuplicates(uProperties);
              if(metaDataHashMap == null) {
                  metaDataHashMap = new ComparableHashMap();
              }
              metaDataHashMap.put("uniqueProperties", uProperties);
              finalEntity.setProperty(metadataProperty, new EmbeddedEntityIterable(metaDataHashMap));

              Iterator<String> it = entityClass.comparableMap().keySet().iterator();
              while (it.hasNext()) {
                String key = it.next();
                Comparable value = entityClass.comparableMap().get(key);
                if (value == null) {
                  if (!key.equals(Constants.RESERVED_FIELD_PUBLICREAD)
                      && !key.equals(Constants.RESERVED_FIELD_PUBLICWRITE)
                      && !key.equals(Constants.RESERVED_FIELD_ACL_WRITE)
                      && !key.equals(Constants.RESERVED_FIELD_ACL_READ)) {
                    finalEntity.deleteProperty(key);
                  }
                } else {
                  if (!key.equals(metadataProperty)
                      && !key.equals(Constants.RESERVED_FIELD_PUBLICREAD)
                      && !key.equals(Constants.RESERVED_FIELD_PUBLICWRITE)
                      && !key.equals(Constants.RESERVED_FIELD_ACL_WRITE)
                      && !key.equals(Constants.RESERVED_FIELD_ACL_READ)
                      && !key.equals(Constants.RESERVED_FIELD_BLOBNAMES)
                      && !key.equals(Constants.RESERVED_FIELD_LINKNAMES)) {
                    //                                if(value instanceof EmbeddedEntityIterable) {
                    //                                    LOG.info(value.toString());
                    //                                }
                    finalEntity.setProperty(key, value);
                  }
                }
              }

              finalEntity.setProperty(Constants.RESERVED_FIELD_DATE_CREATED, getISODate());
              finalEntity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());

              if (entityClass.read() != null) {
                List<String> aclRead = Arrays.asList(entityClass.read());
                // Add User to ACL
                for (String userOrRoleId : aclRead) {
                  if (userOrRoleId != null && !userOrRoleId.isEmpty()) {
                    EntityId userorRoleEntityId = txn.toEntityId(userOrRoleId);
                    Entity userOrRoleEntity = txn.getEntity(userorRoleEntityId);
                    if (userOrRoleEntity != null) {
                      finalEntity.addLink(Constants.RESERVED_FIELD_ACL_READ, userOrRoleEntity);
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
                      finalEntity.addLink(Constants.RESERVED_FIELD_ACL_WRITE, userOrRoleEntity);
                    }
                  }
                }
              }

              if (entityClass.publicRead() != null) {
                finalEntity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, entityClass.publicRead());
              } else {
                finalEntity.deleteProperty(Constants.RESERVED_FIELD_PUBLICREAD);
              }

              if (entityClass.publicWrite() != null) {
                finalEntity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, entityClass.publicWrite());
              } else {
                finalEntity.deleteProperty(Constants.RESERVED_FIELD_PUBLICWRITE);
              }

              entityId[0] = finalEntity.getId().toString();

              Map<String, Comparable> eMap = new LinkedHashMap<>(entityClass.comparableMap());
              eMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId[0]);
              EntityClass created =
                  new EntityClassBuilder().from(entityClass).comparableMap(eMap).build();

              if (actions != null) {
                actions.forEach(
                    action -> {
                      if (action.actionOp().equals(Action.ACTION_OP.LINK)) {
                        String entityType = action.entityType().get();
                        Map<String, Comparable> entityMap = action.entity().get();
                        String linkName = action.linkName().get();
                        String backLinkName = action.backLinkName().get();

                        final Entity linkedEntity = txn.newEntity(entityType);
                        entityMap.forEach(
                            (key, value) -> {
                              linkedEntity.setProperty(key, value);
                            });
                        finalEntity.addLink(linkName, linkedEntity);
                        if (backLinkName != null && !backLinkName.isEmpty()) {
                          linkedEntity.addLink(backLinkName, finalEntity);
                        }

                        Action next = action.next().get();
                        if (next != null) {
                          if (next.actionOp().equals(Action.ACTION_OP.SET)) {
                            String propName = next.propertyName().get();
                            String refPropName = next.referenceProperty().get();
                            Comparable refPropValue = created.comparableMap().get(refPropName);
                            if (propName.equals(Constants.RESERVED_FIELD_ACL_READ)) {
                              EntityId referencedEntity = txn.toEntityId((String) refPropValue);
                              linkedEntity.addLink(
                                  Constants.RESERVED_FIELD_ACL_READ,
                                  txn.getEntity(referencedEntity));
                            } else if (propName.equals(Constants.RESERVED_FIELD_ACL_WRITE)) {
                              EntityId referencedEntity = txn.toEntityId((String) refPropValue);
                              linkedEntity.addLink(
                                  Constants.RESERVED_FIELD_ACL_WRITE,
                                  txn.getEntity(referencedEntity));
                            }
                          }
                        }
                      }
                    });
              }

              if (entityActions != null) {
                entityActions.forEach(
                    action -> {
                      if (action instanceof LinkAction) {
                        LinkAction linkAction = (LinkAction) action;
                        String targetId = linkAction.entityId();
                        String linkName = linkAction.linkName();
                        if (targetId != null && linkName != null) {
                          EntityId targetEntityId = txn.toEntityId(targetId);
                          finalEntity.addLink(linkName, txn.getEntity(targetEntityId));
                        }
                      } else if (action instanceof BacklinkAction) {
                        BacklinkAction backlinkAction = (BacklinkAction) action;
                        String targetId = backlinkAction.entityId();
                        String linkName = backlinkAction.linkName();
                        if (targetId != null && linkName != null) {
                          EntityId sourceEntityId = txn.toEntityId(targetId);
                          Entity source = txn.getEntity(sourceEntityId);
                          source.addLink(linkName, finalEntity);
                        }
                      }
                    });
              }

              if (entityClass.blobName() != null && entityClass.blob() != null) {
                finalEntity.setBlob(entityClass.blobName(), entityClass.blob());
              }
            }
          });
    } finally {
      //// entityStore.close();
    }
    return entityId[0];
  }

  @Override
  public boolean updateEntity(
      String instance,
      String namespace,
      String entityType,
      final String entityId,
      final Map<String, Comparable> comparableMap,
      final String[] read,
      final String[] write,
      final Boolean publicRead,
      final Boolean publicWrite,
      final EntityMetadata metadata) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {

              if (comparableMap != null) {
                comparableMap.remove(metadataProperty);
              }

              final EntityIterable[] iterable = new EntityIterable[1];

              ComparableLinkedList<Comparable> uniqueProperties =
                  (metadata != null && metadata.uniqueProperties() != null)
                      ? Comparables.cast(metadata.uniqueProperties())
                      : new ComparableLinkedList<>();
              ComparableLinkedList<Comparable> uniquePropertyList = null;
              Entity reference = null;
              if (namespace != null) {
                reference =
                    txn.getAll(entityType)
                        .intersect(txn.find(entityType, namespaceProperty, namespace))
                        .getFirst();
              } else {
                reference = txn.getAll(entityType).getFirst();
              }
              if (reference.getProperty(metadataProperty) != null) {
                EmbeddedEntityIterable metadata =
                    (EmbeddedEntityIterable) reference.getProperty(metadataProperty);
                ComparableHashMap comparableHashMap = (ComparableHashMap) metadata.asObject();
                uniquePropertyList =
                    (ComparableLinkedList<Comparable>) comparableHashMap.get("uniqueProperties");
                if (uniquePropertyList == null) {
                  uniquePropertyList = new ComparableLinkedList<>();
                }
                uniquePropertyList.addAll(uniqueProperties);
                removeDuplicates(uniquePropertyList);
              }
              if (metadata != null && metadata.uniqueProperties() != null) {
                uniquePropertyList = new ComparableLinkedList<>();
                uniquePropertyList.addAll(uniqueProperties);
                removeDuplicates(uniquePropertyList);
              }

              if (uniquePropertyList != null) {
                uniquePropertyList.forEach(
                    property -> {
                      Comparable propertyValue = comparableMap.get(property);
                      if (iterable[0] == null && propertyValue != null) {
                        iterable[0] = txn.find(entityType, (String) property, propertyValue);
                      }
                      if (propertyValue != null) {
                        iterable[0] =
                            iterable[0].union(
                                txn.find(entityType, (String) property, propertyValue));
                      }
                    });
              }

              EntityId idOfEntity = txn.toEntityId(entityId);

              if (iterable[0] != null && !iterable[0].isEmpty() && !iterable[0].getFirst().getId().equals(idOfEntity)) {
                throw new IllegalArgumentException("Duplicate value(s) found");
              }

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
                if(publicRead != null) {
                  entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                }
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
                if(publicWrite != null) {
                  entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
                }
              }
              entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
              success[0] = true;
            }
          });
    } finally {
      //// entityStore.close();
    }
    return success[0];
  }

  @Override
  public <T> Map<String, Comparable> getFirstEntity(
      String dir,
      String namespace,
      String kind,
      String propertyKey,
      Comparable<T> propertyVal,
      Class<T> clazz) {
    final Map<String, Comparable> comparableMap = new LinkedHashMap<>();
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, dir);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {

              EntityIterable result = null;
              if (namespace != null && !namespace.isEmpty()) {
                result =
                    txn.findWithProp(kind, namespaceProperty)
                        .intersect(txn.find(kind, namespaceProperty, namespace));
              } else {
                result = txn.getAll(kind).minus(txn.findWithProp(kind, namespaceProperty));
              }

              Entity entity = result.intersect(txn.find(kind, propertyKey, propertyVal)).getFirst();
              for (String property : entity.getPropertyNames()) {
                Comparable value = entity.getProperty(property);
                if (value != null) {
                  if (value instanceof EmbeddedEntityIterable) {
                    comparableMap.put(property, ((EmbeddedEntityIterable) value).asObject());
                  } else if (value instanceof EmbeddedArrayIterable) {
                    comparableMap.put(
                        property, (Comparable) ((EmbeddedArrayIterable) value).asObject());
                  } else {
                    comparableMap.put(property, value);
                  }
                }
              }

              List<EntityStub> aclRead = new ComparableLinkedList<>();
              List<EntityStub> aclWrite = new ComparableLinkedList<>();

              Comparable comparablePublicRead =
                  entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
              Comparable comparablePublicWrite =
                  entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

              Boolean publicRead = null;
              Boolean publicWrite = null;

              if (comparablePublicRead != null) {
                publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
              }

              if (comparablePublicWrite != null) {
                publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);
              }

              for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
              }

              for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                aclWrite.add(
                    new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
              }
              comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.getId().toString());
              comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, Comparables.cast(aclRead));
              comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, Comparables.cast(aclWrite));
              comparableMap.put(
                  Constants.RESERVED_FIELD_BLOBNAMES, Comparables.cast(entity.getBlobNames()));
              comparableMap.put(
                  Constants.RESERVED_FIELD_LINKNAMES, Comparables.cast(entity.getLinkNames()));
              comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
              comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);

              String dateCreated =
                  (entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED) != null
                      ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED)
                      : null);
              String dateUpdated =
                  (entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED) != null
                      ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED)
                      : null);

              comparableMap.put(Constants.RESERVED_FIELD_DATE_CREATED, dateCreated);
              comparableMap.put(Constants.RESERVED_FIELD_DATE_UPDATED, dateUpdated);
            }
          });
    } finally {
      // entityStore.close();
    }
    return comparableMap;
  }

  @Override
  public <T> InputStream getFirstEntityBlob(
      String appId,
      String namespace,
      String kind,
      String propertyKey,
      Comparable<T> propertyVal,
      Class<T> clazz,
      String blobKey) {
    final InputStream[] inputStream = new InputStream[1];
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appId);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityIterable result = null;
              if (namespace != null && !namespace.isEmpty()) {
                result =
                    txn.findWithProp(kind, namespaceProperty)
                        .intersect(txn.find(kind, namespaceProperty, namespace));
              } else {
                result = txn.getAll(kind).minus(txn.findWithProp(kind, namespaceProperty));
              }
              Entity entity = result.intersect(txn.find(kind, propertyKey, propertyVal)).getFirst();
              if (entity != null) {
                inputStream[0] = entity.getBlob(blobKey);
              }
            }
          });
    } finally {
      //// entityStore.close();
    }
    return inputStream[0];
  }

  @Override
  public Map<String, Comparable> getEntity(
      String instance, String namespace, final String entityType, final String entityId, List<String> includes) {
    final Map<String, Comparable> comparableMap = new LinkedHashMap<>();
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);

              for (String property : entity.getPropertyNames()) {
                Comparable value = entity.getProperty(property);
                if (value != null) {
                  if (value instanceof EmbeddedEntityIterable) {
                    comparableMap.put(property, ((EmbeddedEntityIterable) value).asObject());
                  } else if (value instanceof EmbeddedArrayIterable) {
                    comparableMap.put(
                        property, (Comparable) ((EmbeddedArrayIterable) value).asObject());
                  } else {
                    comparableMap.put(property, value);
                  }
                }
              }

              List<EntityStub> aclRead = new LinkedList<>();
              List<EntityStub> aclWrite = new LinkedList<>();

              Comparable comparablePublicRead =
                  entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
              Comparable comparablePublicWrite =
                  entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

              Boolean publicRead = null;
              Boolean publicWrite = null;

              if (comparablePublicRead != null) {
                publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
              }

              if (comparablePublicWrite != null) {
                publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);
              }

              for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
              }

              for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                aclWrite.add(
                    new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
              }
              comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, idOfEntity.toString());
              comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, Comparables.cast(aclRead));
              comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, Comparables.cast(aclWrite));
              comparableMap.put(
                  Constants.RESERVED_FIELD_BLOBNAMES, Comparables.cast(entity.getBlobNames()));
              comparableMap.put(
                  Constants.RESERVED_FIELD_LINKNAMES, Comparables.cast(entity.getLinkNames()));
              comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
              comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);

              String dateCreated =
                  (entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED) != null
                      ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED)
                      : null);
              String dateUpdated =
                  (entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED) != null
                      ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED)
                      : null);

              comparableMap.put(Constants.RESERVED_FIELD_DATE_CREATED, dateCreated);
              comparableMap.put(Constants.RESERVED_FIELD_DATE_UPDATED, dateUpdated);
            }
          });
    } finally {
      //// entityStore.close();
    }
    return comparableMap;
  }

    @Override
    public List<Map<String, Comparable>> getEntity(String instance,
                                                   String namespace,
                                                   String entityType,
                                                   String propertyName,
                                                   Comparable propertyValue, List<String> includes) {
        List<Map<String, Comparable>> entityList = new LinkedList<>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(
                    new StoreTransactionalExecutable() {
                        @Override
                        public void execute(@NotNull final StoreTransaction txn) {

                            EntityIterable entities;
                            if(namespace != null) {
                                entities = txn.find(entityType, propertyName, propertyValue)
                                        .intersect(txn.find(entityType, namespaceProperty, namespace));
                            } else {
                                entities = txn.find(entityType, propertyName, propertyValue)
                                        .minus(txn.findWithProp(entityType, namespaceProperty));
                            }

                            if(entities != null && !entities.isEmpty()) {
                                for(Entity entity : entities) {
                                    Map<String,Comparable> comparableMap = new LinkedHashMap<>();
                                    for (String property : entity.getPropertyNames()) {
                                        Comparable value = entity.getProperty(property);
                                        if (value != null) {
                                            if (value instanceof EmbeddedEntityIterable) {
                                                comparableMap.put(property, ((EmbeddedEntityIterable) value).asObject());
                                            } else if (value instanceof EmbeddedArrayIterable) {
                                                comparableMap.put(
                                                        property, (Comparable) ((EmbeddedArrayIterable) value).asObject());
                                            } else {
                                                comparableMap.put(property, value);
                                            }
                                        }
                                    }

                                    List<EntityStub> aclRead = new LinkedList<>();
                                    List<EntityStub> aclWrite = new LinkedList<>();

                                    Comparable comparablePublicRead =
                                            entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                                    Comparable comparablePublicWrite =
                                            entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                                    Boolean publicRead = null;
                                    Boolean publicWrite = null;

                                    if (comparablePublicRead != null) {
                                        publicRead = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                                    }

                                    if (comparablePublicWrite != null) {
                                        publicWrite = (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);
                                    }

                                    for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                                        aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                                    }

                                    for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                                        aclWrite.add(
                                                new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                                    }
                                    comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.toString());
                                    comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, Comparables.cast(aclRead));
                                    comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, Comparables.cast(aclWrite));
                                    comparableMap.put(
                                            Constants.RESERVED_FIELD_BLOBNAMES, Comparables.cast(entity.getBlobNames()));
                                    comparableMap.put(
                                            Constants.RESERVED_FIELD_LINKNAMES, Comparables.cast(entity.getLinkNames()));
                                    comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
                                    comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);

                                    String dateCreated =
                                            (entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED) != null
                                                    ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED)
                                                    : null);
                                    String dateUpdated =
                                            (entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED) != null
                                                    ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED)
                                                    : null);

                                    comparableMap.put(Constants.RESERVED_FIELD_DATE_CREATED, dateCreated);
                                    comparableMap.put(Constants.RESERVED_FIELD_DATE_UPDATED, dateUpdated);

                                  if(includes != null && !includes.isEmpty()) {
                                      ComparableLinkedList<LinkDTO> linkDTOS = new ComparableLinkedList<LinkDTO>();
                                      for(String include : includes) {
                                      LinkDTO linkDTO = new LinkDTO();
                                      linkDTO.setLinkName(include);
                                      if(include.equals(Constants.RESERVED_FIELD_ACL_WRITE)
                                              || include.equals(Constants.RESERVED_FIELD_ACL_READ)
                                              || include.equals(Constants.ROLE_LINKNAME)
                                              || include.equals(Constants.USERS_LINKNAME)) {
                                        continue;
                                      }
                                      EntityIterable linkedEntities = entity.getLinks(include);
                                      for(Entity linkEntity : linkedEntities) {
                                        EntityDTO entityDTO = entityToEntityDTO(linkEntity.getType(), linkEntity, defaultUserStore);
                                        linkDTO.getEntities().add(entityDTO);
                                      }
                                      linkDTOS.add(linkDTO);
                                    }
                                    comparableMap.put("links", linkDTOS);
                                  }
                                  entityList.add(comparableMap);
                                }

                            }
                        }
                    });
        } finally {
            //// entityStore.close();
        }
        return entityList;
  }

    @Override
  public List<String> getACLReadList(String instance, String namespace, String entityId) {
    final List<String> aclList = new LinkedList<>();
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              EntityIterable links = entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE);
              for (Entity aclWriteLink : links) {
                String id = aclWriteLink.getId().toString();
                aclList.add(id);
              }
            }
          });
    } finally {
      //// entityStore.close();
    }
    return aclList;
  }

  @Override
  public List<String> getACLWriteList(String instance, String namespace, String entityId) {
    final List<String> aclList = new LinkedList<>();
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              EntityIterable links = entity.getLinks(Constants.RESERVED_FIELD_ACL_READ);
              for (Entity aclWriteLink : links) {
                String id = aclWriteLink.getId().toString();
                aclList.add(id);
              }
            }
          });
    } finally {
      //// entityStore.close();
    }
    return aclList;
  }

  @Override
  public boolean isPublicRead(String instance, String namespace, String entityId) {
    final boolean[] isPublicRead = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              Boolean publicRead =
                  (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
              isPublicRead[0] = publicRead;
            }
          });
    } finally {
      //// entityStore.close();
    }
    return isPublicRead[0];
  }

  @Override
  public boolean isPublicWrite(String instance, String namespace, String entityId) {
    final boolean[] isPublicWrite = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              Boolean publicWrite =
                  (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);
              isPublicWrite[0] = publicWrite;
            }
          });
    } finally {
      //// entityStore.close();
    }
    return isPublicWrite[0];
  }

  @Override
  public Comparable getEntityProperty(
      String instance,
      String namespace,
      final String entityType,
      final String entityId,
      final String propertyName) {
    final Comparable[] comparable = new Comparable[1];
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              comparable[0] = entity.getProperty(propertyName);
            }
          });
    } finally {
      //// entityStore.close();
    }
    return comparable[0];
  }

  @Override
  public InputStream getEntityBlob(
      String instance,
      String namespace,
      String entityType,
      final String entityId,
      final String blobKey) {
    final InputStream[] inputStream = new InputStream[1];
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              inputStream[0] = entity.getBlob(blobKey);
            }
          });
    } finally {
      //// entityStore.close();
    }
    return inputStream[0];
  }

  @Override
  public Long countEntityBlobSize(String instance, String namespace, String entityType, String entityId, String blobKey) {
    final Long[] count = new Long[1];
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
              new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                  EntityId idOfEntity = txn.toEntityId(entityId);
                  final Entity entity = txn.getEntity(idOfEntity);
                  count[0] = entity.getBlobSize(blobKey);
                }
              });
    } finally {
      //// entityStore.close();
    }
    return count[0];
  }

  @Override
  public boolean createEntityBlob(
      String instance,
      String namespace,
      String entityType,
      String entityId,
      String blobKey,
      InputStream is) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              entity.setBlob(blobKey, is);
              entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
              success[0] = true;
            }
          });
    } finally {
      //// entityStore.close();
    }
    return success[0];
  }

    @Override
    public boolean createEntityBlob(String instance, String namespace, String entityType, String propertyName, Comparable propertyValue, String blobKey, InputStream is) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(
                    new StoreTransactionalExecutable() {
                        @Override
                        public void execute(@NotNull final StoreTransaction txn) {
                            EntityIterable result = txn.find(entityType, propertyName, propertyValue);
                            if(namespace != null) {
                                result = txn.find(entityType, propertyName, propertyValue).intersect(txn.find(entityType, namespaceProperty, namespace));
                            } else {
                                result = txn.find(entityType, propertyName, propertyValue).minus(txn.findWithProp(entityType, namespaceProperty));
                            }
                            if(!result.isEmpty() && result.size() > 1) {
                                throw new IllegalArgumentException("Multiple entities found");
                            }
                            final Entity entity = result.getFirst();
                            entity.setBlob(blobKey, is);
                            entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
                            success[0] = true;
                        }
                    });
        } finally {
            //// entityStore.close();
        }
        return success[0];
    }

    @Override
  public boolean deleteEntityBlob(
      String instance, String namespace, String entityType, String entityId, String blobKey) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              entity.deleteBlob(blobKey);
              success[0] = true;
            }
          });
    } finally {
      //// entityStore.close();
    }
    return success[0];
  }

  @Override
  public List<String> getLinkNames(
      String instance, String namespace, String entityType, String entityId) {
    final List<String>[] result = new List[] {new LinkedList<>()};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              result[0] = entity.getLinkNames();
            }
          });
    } finally {
      //// entityStore.close();
    }
    return result[0];
  }

  @Override
  public List<String> getBlobKeys(
      String instance, String namespace, String entityType, String entityId) {
    final List<String>[] result = new List[] {new LinkedList<>()};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              result[0] = entity.getBlobNames();
            }
          });
    } finally {
      //// entityStore.close();
    }
    return result[0];
  }

  @Override
  public boolean deleteProperty(
      String instance, String namespace, String entityType, String propertyName) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {

              if (propertyName.equals(metadataProperty)) {
                throw new IllegalArgumentException(
                    metadataProperty + " property cannot be removed");
              }

              EntityIterable entities = txn.findWithProp(entityType, propertyName);
              final boolean[] hasError = {false};
              entities.forEach(
                  entity -> {
                    if (!entity.deleteProperty(propertyName)) {
                      hasError[0] = true;
                    }
                  });
              success[0] = !hasError[0];
            }
          });
    } finally {
      // entityStore.close();
    }
    return success[0];
  }

  @Override
  public boolean updateProperty(
      String instance,
      String namespace,
      String entityType,
      String propertyName,
      EntityMetadata metadata) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {

              if (metadata != null && metadata.uniqueProperties() != null) {
                metadata
                    .uniqueProperties()
                    .forEach(
                        property -> {
                          EntityIterable result = null;
                          if (namespace != null && !namespace.isEmpty()) {
                            result =
                                txn.findWithProp(entityType, namespaceProperty)
                                    .intersect(txn.find(entityType, namespaceProperty, namespace));
                          } else {
                            result =
                                txn.getAll(entityType)
                                    .minus(txn.findWithProp(entityType, namespaceProperty));
                          }
                          Comparable previousVal = null;
                          for (Entity entity : result) {
                            Comparable propertyValue = entity.getProperty(property);
                            if (propertyValue == null) {
                              continue;
                            } else if (propertyValue instanceof String) {
                              if (previousVal != null) {
                                if (previousVal.equals(propertyValue)) {
                                  throw new IllegalArgumentException(
                                      "Found duplicated value on " + entity.getId());
                                }
                              }
                              previousVal = propertyValue;
                            } else if (propertyValue instanceof Boolean) {
                              if (previousVal != null) {
                                if (previousVal.equals(propertyValue)) {
                                  throw new IllegalArgumentException(
                                      "Found duplicated value on " + entity.getId());
                                }
                              }
                              previousVal = propertyValue;
                            } else if (propertyValue instanceof Number) {
                              if (previousVal != null) {
                                if (previousVal.equals(propertyValue)) {
                                  throw new IllegalArgumentException(
                                      "Found duplicated value on " + entity.getId());
                                }
                              }
                              previousVal = propertyValue;
                            } else {
                              throw new IllegalArgumentException(
                                  "Found invalid property value " + entity.getId());
                            }
                          }
                        });
              }

              Entity firstEntity;
              if (namespace != null) {
                firstEntity =
                    txn.getAll(entityType)
                        .intersect(txn.find(entityType, namespaceProperty, namespace))
                        .getFirst();
              } else {
                firstEntity = txn.getAll(entityType).getFirst();
              }
              if (firstEntity != null) {
                LOG.with("propertyNames", firstEntity.getPropertyNames());
                ComparableLinkedList<Comparable> uniqueProperties =
                    Comparables.cast(metadata.uniqueProperties());
                ComparableLinkedList<Comparable> finalUniqueProperties =
                    new ComparableLinkedList<>();
                EmbeddedEntityIterable embeddedMetadata =
                    (EmbeddedEntityIterable) firstEntity.getProperty(metadataProperty);
                ComparableHashMap comparableMetadata = null;
                if (embeddedMetadata != null) {
                  comparableMetadata = ((ComparableHashMap) embeddedMetadata.asObject());
                  if (comparableMetadata != null) {
                    // ComparableLinkedList comparableLinkedList = (ComparableLinkedList)
                    // comparableMetadata.get("uniqueProperties");
                    finalUniqueProperties = new ComparableLinkedList<>();
                    // finalUniqueProperties.addAll(comparableLinkedList);
                    if (finalUniqueProperties != null) {
                      if (uniqueProperties == null) {
                        uniqueProperties = new ComparableLinkedList<>();
                      }
                      finalUniqueProperties.addAll(uniqueProperties);
                      removeDuplicates(finalUniqueProperties);
                      comparableMetadata.put("uniqueProperties", finalUniqueProperties);
                    } else {
                      comparableMetadata.put(
                          "uniqueProperties",
                          new EmbeddedArrayIterable(new ComparableLinkedList<>()));
                    }
                  } else {
                    comparableMetadata = new ComparableHashMap();
                    if (finalUniqueProperties == null) {
                      finalUniqueProperties = new ComparableLinkedList<>();
                    }
                    if (uniqueProperties == null) {
                      uniqueProperties = new ComparableLinkedList<>();
                    }
                    finalUniqueProperties.addAll(uniqueProperties);
                    removeDuplicates(finalUniqueProperties);
                    comparableMetadata.put("uniqueProperties", finalUniqueProperties);
                  }
                } else {
                  comparableMetadata = new ComparableHashMap();
                  finalUniqueProperties.addAll(uniqueProperties);
                  removeDuplicates(finalUniqueProperties);
                  comparableMetadata.put("uniqueProperties", finalUniqueProperties);
                }
                EntityIterable result = null;
                if (namespace != null) {
                  result =
                      txn.getAll(entityType)
                          .intersect(txn.find(entityType, namespaceProperty, namespace));
                } else {
                  result = txn.getAll(entityType);
                }
                ComparableHashMap finalMetadata = comparableMetadata;
                for (Entity entity : result) {
                  entity.setProperty(
                      metadataProperty,
                      new EmbeddedEntityIterable(Comparables.cast(finalMetadata)));
                }
              } else {
                new IllegalArgumentException("Type of entity does not exists");
              }
              success[0] = true;
            }
          });
    } finally {
      // entityStore.close();
    }
    return success[0];
  }

  @Override
  public boolean updateProperty(String instance, String namespace, String entityType, String entityId, String propertyName,
                                Comparable propertyValue, EntityMetadata update) {
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    final boolean[] success = {false};
    try{
      entityStore.executeInTransaction(new StoreTransactionalExecutable() {
        @Override
        public void execute(@NotNull StoreTransaction txn) {
          Entity entity;
          Entity firstEntity;
          if (namespace != null) {
            firstEntity =
                    txn.getAll(entityType)
                            .intersect(txn.find(entityType, namespaceProperty, namespace))
                            .getFirst();
            entity = txn.getEntity(txn.toEntityId(entityId));
          } else {
            firstEntity = txn.getAll(entityType).minus(txn.findWithProp(entityType, namespaceProperty)).getFirst();
            entity = txn.getEntity(txn.toEntityId(entityId));
          }
          List<String> uniquePropList = new LinkedList<>();
          if (firstEntity != null) {
            Comparable metadata = firstEntity.getProperty(metadataProperty);
            if(metadata != null) {
              EmbeddedEntityIterable embeddedEntityIterable = (EmbeddedEntityIterable) metadata;
              ComparableHashMap comparableHashMap = (ComparableHashMap) embeddedEntityIterable.asObject();
              if(comparableHashMap.get("uniqueProperties") != null) {
                ComparableLinkedList comparableLinkedList = (ComparableLinkedList) comparableHashMap.get("uniqueProperties");
                comparableLinkedList.forEach(property -> {
                  uniquePropList.add((String) property);
                });
              }
            }
          }
          if(!uniquePropList.isEmpty()) {
            uniquePropList.forEach(property -> {
              if(namespace != null) {
                EntityIterable result = txn.find(entityType, propertyName, propertyValue)
                        .intersect(txn.findWithProp(entityType, namespace));
                if(!result.isEmpty()) {
                  throw new IllegalArgumentException("Duplicate value found");
                }
              } else {
                EntityIterable result = txn.find(entityType, propertyName, propertyValue)
                        .minus(txn.findWithProp(entityType, namespace));
                if(!result.isEmpty()) {
                  throw new IllegalArgumentException("Duplicate value found");
                }
              }
            });
          }

          if(!isReservedProperty(propertyName)) {
            entity.setProperty(propertyName, propertyValue);
          } else {
            throw new IllegalArgumentException("Cannot modify " + propertyName + " property");
          }

        }
      });
    } finally{
    }
    return success[0];
  }

  @Override
  public List<Map<String, Comparable>> getEntities(
      String instance,
      String namespace,
      String entityType,
      String propertyName,
      Comparable propertyValue,
      int skip,
      int limit, List<String> includes) {
    final List<Map<String, Comparable>> entities = new LinkedList<Map<String, Comparable>>();
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {

              EntityIterable result = null;
              if (namespace != null && !namespace.isEmpty()) {
                result =
                    txn.findWithProp(entityType, namespaceProperty)
                        .intersect(txn.find(entityType, namespaceProperty, namespace));
              } else {
                result =
                    txn.getAll(entityType).minus(txn.findWithProp(entityType, namespaceProperty));
              }
              result =
                  result
                      .intersect(txn.find(entityType, propertyName, propertyValue))
                      .skip(skip)
                      .take(limit);
              result = result.skip(skip).take(limit);
              for (Entity entity : result) {
                final Map<String, Comparable> comparableMap = new LinkedHashMap<>();
                for (String property : entity.getPropertyNames()) {
                  Comparable value = entity.getProperty(property);
                  if (value != null) {
                    if (value != null) {
                      if (value instanceof EmbeddedEntityIterable) {
                        comparableMap.put(property, ((EmbeddedEntityIterable) value).asObject());
                      } else if (value instanceof EmbeddedArrayIterable) {
                        comparableMap.put(
                            property, (Comparable) ((EmbeddedArrayIterable) value).asObject());
                      } else {
                        comparableMap.put(property, value);
                      }
                    }
                  }
                }

                List<EntityStub> aclRead = new LinkedList<>();
                List<EntityStub> aclWrite = new LinkedList<>();

                Boolean publicRead =
                    (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                Boolean publicWrite =
                    (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                  aclRead.add(
                      new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                }

                for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                  aclWrite.add(
                      new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                }

                comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.getId().toString());
                comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, Comparables.cast(aclRead));
                comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, Comparables.cast(aclWrite));
                comparableMap.put(
                    Constants.RESERVED_FIELD_BLOBNAMES, Comparables.cast(entity.getBlobNames()));
                comparableMap.put(
                    Constants.RESERVED_FIELD_LINKNAMES, Comparables.cast(entity.getLinkNames()));
                comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
                if (entity.getType().equals(defaultUserStore)) {
                  comparableMap.remove(Constants.RESERVED_FIELD_PASSWORD);
                }

                String dateCreated =
                    (entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED) != null
                        ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED)
                        : null);
                String dateUpdated =
                    (entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED) != null
                        ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED)
                        : null);

                comparableMap.put(Constants.RESERVED_FIELD_DATE_CREATED, dateCreated);
                comparableMap.put(Constants.RESERVED_FIELD_DATE_UPDATED, dateUpdated);

                entities.add(comparableMap);
              }
            }
          });
    } finally {
      //// entityStore.close();
    }
    return entities;
  }

  @Override
  public boolean deleteEntity(
      String instance, String namespace, String entityType, final String entityId) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              Entity entity = txn.getEntity(idOfEntity);
              entity.getLinkNames().forEach(linkName -> {
                Entity linked = entity.getLink(linkName);
                entity.deleteLink(linkName, linked);
              });

              // TODO: This is a performance issue
              final List<String> allLinkNames = ((PersistentEntityStoreImpl) entityStore).getAllLinkNames((PersistentStoreTransaction) entityStore.getCurrentTransaction());
              for (final String entityType : txn.getEntityTypes()) {
                for (final String linkName : allLinkNames) {
                  for (final Entity referrer : txn.findLinks(entityType, entity, linkName)) {
                    referrer.deleteLink(linkName, entity);
                  }
                }
              }

              success[0] = entity.delete();
            }
          });
    } finally {
      //// entityStore.close();
    }
    return success[0];
  }

  @Override
  public boolean deleteEntities(String instance, String namespace, final String entityType) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityIterable result = null;
              if (namespace != null && !namespace.isEmpty()) {
                result =
                    txn.findWithProp(entityType, namespaceProperty)
                        .intersect(txn.find(entityType, namespaceProperty, namespace));
              } else {
                result =
                    txn.getAll(entityType).minus(txn.findWithProp(entityType, namespaceProperty));
              }
              final boolean[] hasError = {false};
              for (Entity entity : result) {

                entity.getLinkNames().forEach(linkName -> {
                  Entity linked = entity.getLink(linkName);
                  entity.deleteLink(linkName, linked);
                });


                // TODO: This is a performance issue
                final List<String> allLinkNames = ((PersistentEntityStoreImpl) entityStore).getAllLinkNames((PersistentStoreTransaction) entityStore.getCurrentTransaction());
                for (final String entityType : txn.getEntityTypes()) {
                  for (final String linkName : allLinkNames) {
                    for (final Entity referrer : txn.findLinks(entityType, entity, linkName)) {
                      referrer.deleteLink(linkName, entity);
                    }
                  }
                }

                if (!entity.delete()) {
                  hasError[0] = true;
                }
              }
              success[0] = !hasError[0];
            }
          });
    } finally {
      //// entityStore.close();
    }

    return success[0];
  }

  @Override
  public boolean deleteEntityType(String instance, String namespace, String entityType) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      PersistentEntityStoreImpl storeImp = (PersistentEntityStoreImpl) entityStore;

      storeImp.executeInTransaction(new StoreTransactionalExecutable() {
        @Override
        public void execute(@NotNull StoreTransaction txn) {
            EntityIterable result = txn.getAll(entityType);
            for(Entity entity : result) {
              entity.getLinkNames().forEach(linkName -> {
                Entity linked = entity.getLink(linkName);
                entity.deleteLink(linkName, linked);
              });
              if(entityType.equals(defaultUserStore)) {
                // Delete Role links
                EntityIterable roles = entity.getLinks(Constants.ROLE_LINKNAME);
                for(Entity role : roles) {
                  role.deleteLink(Constants.USERS_LINKNAME, entity);
                }
              }

            }
        }
      });

      storeImp.deleteEntityType(entityType);
      success[0] = true;
    } finally {
      //// entityStore.close();
    }
    return success[0];
  }

  @Override
  public boolean linkEntity(
      String instance,
      String namespace,
      String entityType,
      final String linkName,
      final String sourceId,
      final String targetId,
      boolean bSet) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfSource = txn.toEntityId(sourceId);
              EntityId idOfTarget = txn.toEntityId(targetId);
              Entity sourceEntity = txn.getEntity(idOfSource);
              Entity targetEntity = txn.getEntity(idOfTarget);

              sourceEntity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
              targetEntity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());

              if(bSet) {
                  Entity otherEntity = sourceEntity.getLink(linkName);
                  if(otherEntity != null) {
                    otherEntity.deleteLink(linkName, sourceEntity);
                  }
                  sourceEntity.deleteLink(linkName, targetEntity);
                  success[0] = sourceEntity.setLink(linkName, targetEntity);
              } else {
                  success[0] = sourceEntity.addLink(linkName, targetEntity);
              }

            }
          });
    } finally {
      //// entityStore.close();
    }
    return success[0];
  }

  @Override
  public boolean unlinkEntity(
      String instance,
      String namespace,
      String entityType,
      final String linkName,
      final String entityId,
      final String targetId) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId sourceEntityId = txn.toEntityId(entityId);
              EntityId targetEntityId = txn.toEntityId(targetId);
              Entity sourceEntity = txn.getEntity(sourceEntityId);
              Entity targetEntity = txn.getEntity(targetEntityId);

              sourceEntity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
              targetEntity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());

              success[0] = sourceEntity.deleteLink(linkName, targetEntity);
            }
          });
    } finally {
      //// entityStore.close();
    }
    return success[0];
  }

  @Override
  public boolean isLinked(
      String instance,
      String namespace,
      String entityType,
      final String linkName,
      final String entityId,
      final String targetId) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
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
      //// entityStore.close();
    }
    return success[0];
  }

  @Override
  public Map<String, Comparable> getFirstLinkedEntity(
      String instance,
      String namespace,
      String entityType,
      final String entityId,
      final String linkName) {
    final Map<String, Comparable> comparableMap = new LinkedHashMap<>();
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId sourceEntityId = txn.toEntityId(entityId);
              final Entity source = txn.getEntity(sourceEntityId);

              Entity entity = source.getLink(linkName);

              for (String property : entity.getPropertyNames()) {
                Comparable value = entity.getProperty(property);
                if (value != null) {
                  comparableMap.put(property, value);
                }
              }

              List<EntityStub> aclRead = new LinkedList<>();
              List<EntityStub> aclWrite = new LinkedList<>();

              Boolean publicRead =
                  (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
              Boolean publicWrite =
                  (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

              for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
              }

              for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                aclWrite.add(
                    new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
              }

              comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.getId().toString());
              comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, Comparables.cast(aclRead));
              comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, Comparables.cast(aclWrite));
              comparableMap.put(
                  Constants.RESERVED_FIELD_BLOBNAMES, Comparables.cast(entity.getBlobNames()));
              comparableMap.put(
                  Constants.RESERVED_FIELD_LINKNAMES, Comparables.cast(entity.getLinkNames()));
              comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
              comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);

              String dateCreated =
                  (entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED) != null
                      ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED)
                      : null);
              String dateUpdated =
                  (entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED) != null
                      ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED)
                      : null);

              comparableMap.put(Constants.RESERVED_FIELD_DATE_CREATED, dateCreated);
              comparableMap.put(Constants.RESERVED_FIELD_DATE_UPDATED, dateUpdated);
            }
          });
    } finally {
      //// entityStore.close();
    }
    return comparableMap;
  }

  @Override
  public List<Map<String, Comparable>> getLinkedEntities(
      String instance,
      String namespace,
      String entityType,
      final String entityId,
      final String linkName) {
    final List<Map<String, Comparable>> entities = new LinkedList<Map<String, Comparable>>();
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              Entity txnEntity = txn.getEntity(idOfEntity);
              EntityIterable result = txnEntity.getLinks(Arrays.asList(new String[] {linkName}));
              for (Entity entity : result) {
                final Map<String, Comparable> comparableMap = new LinkedHashMap<>();
                for (String property : entity.getPropertyNames()) {
                  Comparable value = entity.getProperty(property);
                  if (value != null) {
                    if (value != null) {
                      if (value instanceof EmbeddedEntityIterable) {
                        comparableMap.put(property, ((EmbeddedEntityIterable) value).asObject());
                      } else if (value instanceof EmbeddedArrayIterable) {
                        comparableMap.put(
                            property, (Comparable) ((EmbeddedArrayIterable) value).asObject());
                      } else {
                        comparableMap.put(property, value);
                      }
                    }
                  }
                }

                List<EntityStub> aclRead = new LinkedList<>();
                List<EntityStub> aclWrite = new LinkedList<>();

                Boolean publicRead =
                    (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                Boolean publicWrite =
                    (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                  aclRead.add(
                      new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                }

                for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                  aclWrite.add(
                      new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                }

                comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.getId().toString());
                comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, Comparables.cast(aclRead));
                comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, Comparables.cast(aclWrite));
                comparableMap.put(
                    Constants.RESERVED_FIELD_BLOBNAMES, Comparables.cast(entity.getBlobNames()));
                comparableMap.put(
                    Constants.RESERVED_FIELD_LINKNAMES, Comparables.cast(entity.getLinkNames()));
                comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);

                if (entity.getType().equals(defaultUserStore)) {
                  comparableMap.remove(Constants.RESERVED_FIELD_PASSWORD);
                }
                comparableMap.put("entityType", entity.getType());

                String dateCreated =
                    (entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED) != null
                        ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED)
                        : null);
                String dateUpdated =
                    (entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED) != null
                        ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED)
                        : null);

                comparableMap.put(Constants.RESERVED_FIELD_DATE_CREATED, dateCreated);
                comparableMap.put(Constants.RESERVED_FIELD_DATE_UPDATED, dateUpdated);

                entities.add(comparableMap);
              }
            }
          });
    } finally {

    }
    return entities;
  }

  @Override
  public List<Map<String, Comparable>> listEntities(
      String instance,
      String namespace,
      String entityType,
      String userIdRoleId,
      int skip,
      int limit,
      String sort,
      String linkName,
      String linkedTo,
      boolean isMasterKey,
      List<TransactionFilter> filters) {
    final List<Map<String, Comparable>> entities = new LinkedList<Map<String, Comparable>>();
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityIterable result = null;
              if (namespace != null && !namespace.isEmpty()) {
                result =
                    txn.findWithProp(entityType, namespaceProperty)
                        .intersect(txn.find(entityType, namespaceProperty, namespace));
              } else {
                result =
                    txn.getAll(entityType).minus(txn.findWithProp(entityType, namespaceProperty));
              }
              if (isMasterKey) {
                result = result.skip(skip).take(limit);
                if (filters != null && !filters.isEmpty()) {
                  result = filter(entityType, result, filters, txn);
                }
              } else if (userIdRoleId == null) {
                result = txn.find(entityType, "publicRead", true);
                long count = result.count();
                LOG.info("COUNT: " + count);
                if (filters != null && !filters.isEmpty()) {
                  result = filter(entityType, result, filters, txn);
                }
                if (sort != null) {
                  if (sort.startsWith("-")) {
                    String sortDescending = sort.substring(1);
                    result = txn.sort(entityType, sortDescending, result, false);
                  } else {
                    String sortAscending = sort.substring(1);
                    result = txn.sort(entityType, sortAscending, result, true);
                  }
                }

              } else {
                Entity targetEntity = txn.getEntity(txn.toEntityId(userIdRoleId));

                List<Role> roles = new LinkedList<>();
                for (Entity roleEntity : targetEntity.getLinks(Constants.ROLE_LINKNAME)) {
                  Role role = new Role();
                  role.setEntityId(roleEntity.getId().toString());
                  role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                  roles.add(role);
                }

                if(roles != null && !roles.isEmpty()) {
                  result =
                          txn.findLinks(entityType, targetEntity, "aclRead")
                                  .concat(txn.find(entityType, "publicRead", true));
                  for(Role role : roles) {
                    Entity roleEntity = txn.getEntity(txn.toEntityId(role.getEntityId()));
                    result =
                            txn.findLinks(entityType, roleEntity, "aclRead")
                                    .concat(txn.find(entityType, "publicRead", true));
                  }
                } else {
                  result =
                          txn.findLinks(entityType, targetEntity, "aclRead")
                                  .concat(txn.find(entityType, "publicRead", true));
                }


                if (filters != null && !filters.isEmpty()) {
                  EntityIterable filtered = filter(entityType, result, filters, txn);
                  result = result.intersect(filtered);
                }

                if(linkedTo != null) {
                  EntityId targetId = txn.toEntityId(linkedTo);
                  Entity target = txn.getEntity(targetId);
                  EntityIterable linkedEntities = target.getLinks(linkName);
                  EntityIterable all = txn.getAll(entityType);
                  // Make sure to get only get linked entities of the required type
                  EntityIterable filtered = all.intersect(linkedEntities);
                  result = result.intersect(filtered);
                }

                if (sort != null) {
                  if (sort.startsWith("-")) {
                    String sortDescending = sort.substring(1);
                    result = txn.sort(entityType, sortDescending, result, false);
                  } else {
                    String sortAscending = sort.substring(1);
                    result = txn.sort(entityType, sortAscending, result, true);
                  }
                }
              }
              result = result.skip(skip).take(limit);
              for (Entity entity : result) {
                final Map<String, Comparable> comparableMap = new LinkedHashMap<>();
                for (String property : entity.getPropertyNames()) {
                  Comparable value = entity.getProperty(property);
                  if (value != null) {
                    if (value != null) {
                      if (value instanceof EmbeddedEntityIterable) {
                        comparableMap.put(property, ((EmbeddedEntityIterable) value).asObject());
                      } else if (value instanceof EmbeddedArrayIterable) {
                        comparableMap.put(
                            property, Comparables.cast((((EmbeddedArrayIterable) value).asObject())));
                      } else {
                        comparableMap.put(property, value);
                      }
                    }
                  }
                }

                List<EntityStub> aclRead = new LinkedList<>();
                List<EntityStub> aclWrite = new LinkedList<>();

                Boolean publicRead =
                    (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                Boolean publicWrite =
                    (Boolean) entity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                for (Entity aclReadLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                  aclRead.add(
                      new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                }

                for (Entity aclWriteLink : entity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                  aclWrite.add(
                      new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                }

                comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entity.getId().toString());
                comparableMap.put(Constants.RESERVED_FIELD_ACL_READ, Comparables.cast(aclRead));
                comparableMap.put(Constants.RESERVED_FIELD_ACL_WRITE, Comparables.cast(aclWrite));
                comparableMap.put(
                    Constants.RESERVED_FIELD_BLOBNAMES, Comparables.cast(entity.getBlobNames()));
                comparableMap.put(
                    Constants.RESERVED_FIELD_LINKNAMES, Comparables.cast(entity.getLinkNames()));
                comparableMap.put(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
                comparableMap.put(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);

                String dateCreated =
                    (entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED) != null
                        ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED)
                        : null);
                String dateUpdated =
                    (entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED) != null
                        ? (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED)
                        : null);

                comparableMap.put(Constants.RESERVED_FIELD_DATE_CREATED, dateCreated);
                comparableMap.put(Constants.RESERVED_FIELD_DATE_UPDATED, dateUpdated);

                if (entity.getType().equals(defaultUserStore)) {
                  comparableMap.remove(Constants.RESERVED_FIELD_PASSWORD);
                }
                comparableMap.remove("nameSpace");
                entities.add(comparableMap);
              }
            }
          });
    } finally {
      //// entityStore.close();
    }
    return entities;
  }

  @Override
  public Long countEntities(String instance, String namespace, String entityType, boolean isMasterKey, List<TransactionFilter> filters) {
    final Long[] count = {0L};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityIterable result = null;
              if (namespace != null && !namespace.isEmpty()) {
                result =
                    txn.findWithProp(entityType, namespaceProperty)
                        .intersect(txn.find(entityType, namespaceProperty, namespace));
              } else {
                result =
                    txn.getAll(entityType).minus(txn.findWithProp(entityType, namespaceProperty));
              }
              if (filters != null && !filters.isEmpty()) {
                result = filter(entityType, result, filters, txn);
              }
              count[0] = result.count();
            }
          });
    } finally {
      //// entityStore.close();
    }
    return count[0];
  }

  @Override
  protected String getDefaultRoleStore() {
    return defaultRoleStore;
  }

}
