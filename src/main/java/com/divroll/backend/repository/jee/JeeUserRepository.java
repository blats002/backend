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
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * GPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.repository.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.*;
import com.divroll.backend.model.action.Action;
import com.divroll.backend.model.builder.EntityClass;
import com.divroll.backend.model.EntityDTO;
import com.divroll.backend.model.filter.TransactionFilter;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.xodus.XodusManager;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.*;
import org.jetbrains.annotations.NotNull;
import scala.actors.threadpool.Arrays;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeUserRepository extends JeeBaseRespository implements UserRepository {

  private static final Logger LOG = LoggerFactory.getLogger(JeeUserRepository.class);

  @Inject
  @Named("defaultRoleStore")
  String defaultRoleStore;

  @Inject
  @Named("defaultUserStore")
  String defaultUserStore;

  @Inject
  @Named("xodusRoot")
  String xodusRoot;

  @Inject
  @Named("namespaceProperty")
  String namespaceProperty;

  @Inject XodusManager manager;

  @Override
  public String createUser(
      String instance,
      String namespace,
      final String entityType,
      final String username,
      final String password,
      final Map<String, Comparable> comparableMap,
      final String[] read,
      final String[] write,
      final Boolean publicRead,
      final Boolean publicWrite,
      String[] roles,
      List<Action> actions,
      EntityClass linkedEntity,
      String linkName,
      String backlinkName) {
    final String[] entityId = {null};

    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);

    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              final Entity entity = txn.newEntity(entityType);

              entity.setProperty(Constants.RESERVED_FIELD_USERNAME, username);
              entity.setProperty(Constants.RESERVED_FIELD_PASSWORD, password);

              if (namespace != null && !namespace.isEmpty()) {
                entity.setProperty(namespaceProperty, namespace);
              }

              if (read != null) {
                List<String> aclRead = Arrays.asList(read);
                // Add User to ACL
                for (String userId : aclRead) {
                  EntityId userEntityId = txn.toEntityId(userId);
                  Entity userOrRoleEntity = txn.getEntity(userEntityId);
                  if (userOrRoleEntity != null) {
                    entity.addLink(Constants.RESERVED_FIELD_ACL_READ, userOrRoleEntity);
                  }
                }
              }

              if(publicRead != null) {
                entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);
              }

              if (write != null) {
                List<String> aclWrite = Arrays.asList(write);
                // Add User to ACL
                for (String userId : aclWrite) {
                  EntityId userEntityId = txn.toEntityId(userId);
                  Entity userOrRoleEntity = txn.getEntity(userEntityId);
                  if (userOrRoleEntity != null) {
                    entity.addLink(Constants.RESERVED_FIELD_ACL_WRITE, userOrRoleEntity);
                  }
                }
              }

              if(publicWrite != null) {
                entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
              }

              for (Object roleId : Arrays.asList(roles)) {
                String id = (String) roleId;
                EntityId roleEntityId = txn.toEntityId(id);
                Entity roleEntity = txn.getEntity(roleEntityId);
                if (roleEntity != null) {
                  entity.addLink(Constants.ROLE_LINKNAME, roleEntity);
                  roleEntity.addLink(Constants.USERS_LINKNAME, entity);
                }
              }

              if (actions != null) {
                actions.forEach(
                    action -> {
                      if (action.actionOp().equals(Action.ACTION_OP.LINK)) {
                        String entityType = action.entityType().get();
                        Map<String, Comparable> entityMap = action.entity().get();
                        String linkName = action.linkName().get();
                        String backLinkName = action.backLinkName().get();

                        final Entity linkedEntity = txn.newEntity(entityType);

                        if (namespace != null && !namespace.isEmpty()) {
                          linkedEntity.setProperty(namespaceProperty, namespace);
                        }

                        entityMap.forEach(
                            (key, value) -> {
                              linkedEntity.setProperty(key, value);
                            });
                        entity.addLink(linkName, linkedEntity);
                        if (backLinkName != null && !backLinkName.isEmpty()) {
                          linkedEntity.addLink(backLinkName, entity);
                        }
                        Action next = action.next().get();
                        if (next != null) {
                          if (next.actionOp().equals(Action.ACTION_OP.SET)) {
                            String propName = next.propertyName().get();
                            String refPropName = next.referenceProperty().get();
                            Comparable refPropValue = entity.getProperty(refPropName);
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

              entity.setProperty(Constants.RESERVED_FIELD_DATE_CREATED, getISODate());
              entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());

              if (linkedEntity != null && linkName != null) {
                Entity otherEntity = txn.newEntity(linkedEntity.entityType());
                if (namespace != null && !namespace.isEmpty()) {
                  otherEntity.setProperty(namespaceProperty, namespace);
                }
                Iterator<String> it = linkedEntity.comparableMap().keySet().iterator();
                while (it.hasNext()) {
                  String key = it.next();
                  Comparable value = linkedEntity.comparableMap().get(key);
                  if (value == null) {
                    if (!key.equals(Constants.RESERVED_FIELD_PUBLICREAD)
                        && !key.equals(Constants.RESERVED_FIELD_PUBLICWRITE)
                        && !key.equals(Constants.RESERVED_FIELD_ACL_WRITE)
                        && !key.equals(Constants.RESERVED_FIELD_ACL_READ)) {
                      otherEntity.deleteProperty(key);
                    }
                  } else {
                    if (!key.equals(Constants.RESERVED_FIELD_PUBLICREAD)
                        && !key.equals(Constants.RESERVED_FIELD_PUBLICWRITE)
                        && !key.equals(Constants.RESERVED_FIELD_ACL_WRITE)
                        && !key.equals(Constants.RESERVED_FIELD_ACL_READ)
                        && !key.equals(Constants.RESERVED_FIELD_BLOBNAMES)
                        && !key.equals(Constants.RESERVED_FIELD_LINKNAMES)) {
                      //                                if(value instanceof EmbeddedEntityIterable)
                      // {
                      //                                    LOG.info(value.toString());
                      //                                }
                      otherEntity.setProperty(key, value);
                    }
                  }
                }

                Entity otherEntityReference = txn.getAll(linkedEntity.entityType()).getFirst();
                Comparable metaData = otherEntityReference.getProperty(Constants.RESERVED_FIELD_METADATA);
                otherEntity.setProperty(Constants.RESERVED_FIELD_METADATA, metaData);

                otherEntity.addLink(Constants.RESERVED_FIELD_ACL_READ, entity);
                otherEntity.addLink(Constants.RESERVED_FIELD_ACL_WRITE, entity);

                if(linkedEntity.write() != null) {
                    String[] write = linkedEntity.write();
                    Arrays.asList(write).forEach(entityId -> {
                        EntityId idOfEntity = txn.toEntityId((String) entityId);
                        Entity aclEntity = txn.getEntity(idOfEntity);
                        if(aclEntity != null) {
                            otherEntity.addLink(Constants.RESERVED_FIELD_ACL_WRITE, aclEntity);
                        }
                    });
                }

                  if(linkedEntity.read() != null) {
                      String[] read = linkedEntity.read();
                      Arrays.asList(read).forEach(entityId -> {
                          EntityId idOfEntity = txn.toEntityId((String) entityId);
                          Entity aclEntity = txn.getEntity(idOfEntity);
                          if(aclEntity != null) {
                              otherEntity.addLink(Constants.RESERVED_FIELD_ACL_READ, aclEntity);
                          }
                      });
                  }

                if(linkedEntity.publicRead() != null) {
                    otherEntity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, linkedEntity.publicRead());
                }

                if(linkedEntity.publicWrite() != null) {
                    otherEntity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, linkedEntity.publicWrite());
                }

                otherEntity.setProperty(Constants.RESERVED_FIELD_DATE_CREATED, getISODate());
                otherEntity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());

                entity.setLink(linkName, otherEntity);
                if (backlinkName != null) {
                  otherEntity.setLink(backlinkName, entity);
                }
              }

              entityId[0] = entity.getId().toString();
            }
          });
    } finally {
      // entityStore.close();
    }
    return entityId[0];
  }

  @Override
  public boolean updateUser(
      String instance,
      String namespace,
      String entityType,
      final String userId,
      final String newUsername,
      final String newPassword,
      final Map<String, Comparable> comparableMap,
      final String[] read,
      final String[] write,
      final Boolean publicRead,
      final Boolean publicWrite,
      String[] roles) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(userId);

              final Entity entity = txn.getEntity(idOfEntity);
              if (newUsername != null) {
                entity.setProperty(Constants.RESERVED_FIELD_USERNAME, newUsername);
              }
              if (newPassword != null) {
                entity.setProperty(Constants.RESERVED_FIELD_PASSWORD, newPassword);
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
              }

              if(publicRead != null) {
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
              }

              if(publicWrite != null) {
                entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);
              }

              List<String> roleList = Arrays.asList(roles);
              if (!roleList.isEmpty()) {
                entity.deleteLinks(Constants.ROLE_LINKNAME);
              }
              for (String roleId : roleList) {
                if (roleId != null && !roleId.isEmpty()) {
                  EntityId roleEntityId = txn.toEntityId(roleId);
                  Entity roleEntity = txn.getEntity(roleEntityId);
                  if (roleEntity != null) {
                    entity.addLink(Constants.ROLE_LINKNAME, roleEntity);
                    roleEntity.addLink(Constants.USERS_LINKNAME, entity);
                  }
                }
              }

              // Update missing role link
              EntityIterable roleEntities = entity.getLinks(Constants.ROLE_LINKNAME);
              for(Entity roleEntity : roleEntities) {
                roleEntity.addLink(Constants.USERS_LINKNAME, entity);
              }

              entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
              success[0] = true;
            }
          });
    } finally {
      // entityStore.close();
    }
    return success[0];
  }

  @Override
  public boolean updateUserPassword(
      String instance, String namespace, String entityType, String entityId, String newPassword) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId idOfEntity = txn.toEntityId(entityId);
              final Entity entity = txn.getEntity(idOfEntity);
              if (newPassword != null) {
                entity.setProperty(Constants.RESERVED_FIELD_PASSWORD, newPassword);
              }
              entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
              success[0] = true;
            }
          });
    } finally {
      // entityStore.close();
    }
    return success[0];
  }

  @Override
  public boolean updateUser(
      String instance,
      String namespace,
      String entityType,
      String entityId,
      Map<String, Comparable> comparableMap,
      String[] read,
      String[] write,
      Boolean publicRead,
      Boolean publicWrite) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
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

              // Update missing role link
              EntityIterable roleEntities = entity.getLinks(Constants.ROLE_LINKNAME);
              for(Entity roleEntity : roleEntities) {
                roleEntity.addLink(Constants.USERS_LINKNAME, entity);
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
  public User getUser(String instance, String namespace, String entityType, final String userID, List<String> includeLinks) {
    final User[] entity = {null};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId userEntityId = txn.toEntityId(userID);
              if (userEntityId != null) {
                final Entity userEntity = txn.getEntity(userEntityId);
                User user = new User();
                user.setUsername((String) userEntity.getProperty(Constants.QUERY_USERNAME));
                user.setPassword((String) userEntity.getProperty(Constants.QUERY_PASSWORD));
                user.setEntityId(userEntity.getId().toString());
                user.setEmail(
                    userEntity.getProperty("email") != null
                        ? (String) userEntity.getProperty("email")
                        : null);

                for (Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                  Role role = new Role();
                  role.setEntityId(roleEntity.getId().toString());
                  role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                  role.setPublicRead(
                      (Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD));
                  role.setPublicWrite(
                      (Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE));

                  List<EntityStub> aclRead = new LinkedList<>();
                  List<EntityStub> aclWrite = new LinkedList<>();

                  for (Entity aclReadLink :
                      roleEntity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                    aclRead.add(
                        new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                  }

                  for (Entity aclWriteLink :
                      roleEntity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                    aclWrite.add(
                        new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                  }

                  role.setAclRead(aclRead);
                  role.setAclWrite(aclWrite);
                  user.getRoles().add(role);
                }

                List<Role> roles = new LinkedList<>();

                List<EntityStub> aclRead = new LinkedList<>();
                List<EntityStub> aclWrite = new LinkedList<>();

                for (Entity aclReadLink : userEntity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                  aclRead.add(
                      new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                }

                for (Entity aclWriteLink :
                    userEntity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                  aclWrite.add(
                      new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                }
                user.setAclRead(aclRead);
                user.setAclWrite(aclWrite);

                for (Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                  Role role = new Role();
                  role.setEntityId(roleEntity.getId().toString());
                  role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                  roles.add(role);
                }

                user.setRoles(roles);

                Boolean publicRead =
                    (Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                Boolean publicWrite =
                    (Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                String dateCreated =
                    (userEntity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED) != null
                        ? (String) userEntity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED)
                        : null);
                String dateUpdated =
                    (userEntity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED) != null
                        ? (String) userEntity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED)
                        : null);

                user.setDateCreated(dateCreated);
                user.setDateUpdated(dateUpdated);

                user.setPublicWrite(publicWrite);
                user.setPublicRead(publicRead);

                if(includeLinks != null && !includeLinks.isEmpty()) {
                  List<Link> links = new LinkedList<>();
                  for(String include : includeLinks) {
                    Link link = new Link();
                    link.setLinkName(include);
                    if(include.equals(Constants.RESERVED_FIELD_ACL_WRITE)
                            || include.equals(Constants.RESERVED_FIELD_ACL_READ)
                            || include.equals(Constants.ROLE_LINKNAME)
                            || include.equals(Constants.USERS_LINKNAME)) {
                      continue;
                    }
                    EntityIterable linkedEntities = userEntity.getLinks(include);
                    for(Entity linkEntity : linkedEntities) {
                      EntityDTO entityDTO = entityToEntityDTO(linkEntity.getType(), linkEntity, defaultUserStore);
                      EntityStub entityStub = new EntityStub(entityDTO.getEntityId(), entityDTO.getEntityType());
                      link.getEntities().add(entityStub);
                    }
                    links.add(link);
                  }
                  user.setLinks(links);
                }
                user.setBlobNames(userEntity.getBlobNames());
                entity[0] = user;
              }
            }
          });
    } finally {
      // entityStore.close();
    }
    return entity[0];
  }

  @Override
  public User getUserByUsername(
      String instance, String namespace, final String entityType, final String username, List<String> includeLinks) {
    final User[] entity = {null};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {

              EntityIterable result;
              if (namespace != null && !namespace.isEmpty()) {
                result =
                    txn.findWithProp(entityType, namespaceProperty)
                        .intersect(txn.find(entityType, namespaceProperty, namespace));
              } else {
                result =
                    txn.getAll(entityType).minus(txn.findWithProp(entityType, namespaceProperty));
              }

              final Entity userEntity =
                  result
                      .intersect(txn.find(entityType, Constants.QUERY_USERNAME, username))
                      .getFirst();
              if (userEntity != null) {
                User user = new User();
                user.setUsername(
                    (String) userEntity.getProperty(Constants.RESERVED_FIELD_USERNAME));
                user.setPassword(
                    (String) userEntity.getProperty(Constants.RESERVED_FIELD_PASSWORD));
                user.setEntityId(userEntity.getId().toString());
                user.setEmail(
                    userEntity.getProperty("email") != null
                        ? (String) userEntity.getProperty("email")
                        : null);
                user.setBlobNames(userEntity.getBlobNames());

                List<Role> roles = new LinkedList<>();
                for (Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                  Role role = new Role();
                  role.setEntityId(roleEntity.getId().toString());
                  role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                  roles.add(role);
                }
                user.setRoles(roles);

                String dateCreated =
                    (userEntity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED) != null
                        ? (String) userEntity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED)
                        : null);
                String dateUpdated =
                    (userEntity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED) != null
                        ? (String) userEntity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED)
                        : null);

                user.setDateCreated(dateCreated);
                user.setDateUpdated(dateUpdated);

                if(includeLinks != null && !includeLinks.isEmpty()) {
                  List<Link> links = new LinkedList<>();
                  for(String include : includeLinks) {
                    Link link = new Link();
                    link.setLinkName(include);
                    if(include.equals(Constants.RESERVED_FIELD_ACL_WRITE)
                            || include.equals(Constants.RESERVED_FIELD_ACL_READ)
                            || include.equals(Constants.ROLE_LINKNAME)
                            || include.equals(Constants.USERS_LINKNAME)) {
                      continue;
                    }
                    EntityIterable linkedEntities = userEntity.getLinks(include);
                    for(Entity linkEntity : linkedEntities) {
                      EntityDTO entityDTO = entityToEntityDTO(linkEntity.getType(), linkEntity, defaultUserStore);
                      EntityStub entityStub = new EntityStub(entityDTO.getEntityId(), entityDTO.getEntityType());
                      link.getEntities().add(entityStub);
                    }
                    links.add(link);
                  }
                  user.setLinks(links);
                }

                entity[0] = user;
              }
            }
          });
    } finally {
      // entityStore.close();
    }
    return entity[0];
  }

  @Override
  public boolean deleteUser(
      String instance, String namespace, String entityType, final String userID) {
    final boolean[] success = {false};
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    try {
      entityStore.executeInTransaction(
          new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
              EntityId roleEntityId = txn.toEntityId(userID);
              Entity entity = txn.getEntity(roleEntityId);

              entity.getLinkNames().forEach(linkName -> {
                Entity linked = entity.getLink(linkName);
                entity.deleteLink(linkName, linked);
              });

              // Delete Role links
              EntityIterable roles = entity.getLinks(Constants.ROLE_LINKNAME);
              for(Entity role : roles) {
                role.deleteLink(Constants.USERS_LINKNAME, entity);
              }

              success[0] = entity.delete();
            }
          });
    } finally {
      // entityStore.close();
    }
    return success[0];
  }

  @Override
  public List<User> listUsers(
      String instance,
      String namespace,
      String entityType,
      String userIdRoleId,
      int skip,
      int limit,
      final String sort,
      boolean isMasterKey,
      List<String> roleNames,
      List<TransactionFilter> filters, List<String> includeLinks) {
    final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
    final List<User> users = new LinkedList<>();
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
                  result = filter(entityType, result, filters, txn);
                }

                if(roleNames != null && !roleNames.isEmpty()) {
                  for(String roleName : roleNames){
                    Entity roleEntity = txn.find(defaultRoleStore, "name", roleName).getFirst();
                    EntityIterable entitiesWithGivenRole = roleEntity.getLinks(Constants.USERS_LINKNAME);
                    result = result.intersect(entitiesWithGivenRole);
                  }
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

              for (Entity userEntity : result) {
                User user = new User();
                user.setEntityId((String) userEntity.getId().toString());
                user.setUsername(
                    (String) userEntity.getProperty(Constants.RESERVED_FIELD_USERNAME));
                user.setEmail((String) userEntity.getProperty("email"));
                user.setBlobNames(userEntity.getBlobNames());

                for (Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                  Role role = new Role();
                  role.setEntityId(roleEntity.getId().toString());
                  role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                  role.setPublicRead(
                      (Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD));
                  role.setPublicWrite(
                      (Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE));

                  List<EntityStub> aclRead = new LinkedList<>();
                  List<EntityStub> aclWrite = new LinkedList<>();

                  for (Entity aclReadLink :
                      roleEntity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                    aclRead.add(
                        new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                  }

                  for (Entity aclWriteLink :
                      roleEntity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                    aclWrite.add(
                        new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                  }

                  role.setAclRead(aclRead);
                  role.setAclWrite(aclWrite);
                  user.getRoles().add(role);
                }

                List<EntityStub> aclRead = new LinkedList<>();
                List<EntityStub> aclWrite = new LinkedList<>();

                for (Entity aclReadLink : userEntity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                  aclRead.add(
                      new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                }

                for (Entity aclWriteLink :
                    userEntity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                  aclWrite.add(
                      new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                }

                user.setAclRead(aclRead);
                user.setAclWrite(aclWrite);
                user.setPublicRead(
                    (Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD));
                user.setPublicWrite(
                    (Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE));

                String dateCreated =
                    (userEntity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED) != null
                        ? (String) userEntity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED)
                        : null);
                String dateUpdated =
                    (userEntity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED) != null
                        ? (String) userEntity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED)
                        : null);

                user.setDateCreated(dateCreated);
                user.setDateUpdated(dateUpdated);

                List<Role> roles = new LinkedList<>();
                for (Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                  Role role = new Role();
                  role.setEntityId(roleEntity.getId().toString());
                  role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                  roles.add(role);
                }
                user.setRoles(roles);

                if(includeLinks != null && !includeLinks.isEmpty()) {
                    List<Link> links = new LinkedList<>();
                  for(String include : includeLinks) {
                    Link link = new Link();
                    link.setLinkName(include);
                    if(include.equals(Constants.RESERVED_FIELD_ACL_WRITE)
                            || include.equals(Constants.RESERVED_FIELD_ACL_READ)
                            || include.equals(Constants.ROLE_LINKNAME)
                            || include.equals(Constants.USERS_LINKNAME)) {
                      continue;
                    }
                    EntityIterable linkedEntities = userEntity.getLinks(include);
                    for(Entity linkEntity : linkedEntities) {
                      EntityDTO entityDTO = entityToEntityDTO(linkEntity.getType(), linkEntity, defaultUserStore);
                      EntityStub entityStub = new EntityStub(entityDTO.getEntityId(), entityDTO.getEntityType());
                      link.getEntities().add(entityStub);
                    }
                    links.add(link);
                  }
                  user.setLinks(links);
                }
                users.add(user);
              }
            }
          });

    } finally {

    }
    return users;
  }

  @Override
  protected String getDefaultRoleStore() {
    return defaultRoleStore;
  }
}
