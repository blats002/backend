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
import com.divroll.backend.model.EntityStub;
import com.divroll.backend.model.Role;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.xodus.XodusManager;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.*;
import org.jetbrains.annotations.NotNull;
import scala.actors.threadpool.Arrays;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeRoleRepository implements RoleRepository {

    private static final Logger LOG
            = Logger.getLogger(JeeRoleRepository.class.getName());

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    XodusManager manager;

    @Override
    public String createRole(final String instance, final String storeName, final String roleName,
                             final String[] read, final String[] write, final Boolean publicRead, final Boolean publicWrite) {
        final String[] entityId = {null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final Entity entity = txn.newEntity(storeName);
                    entity.setProperty(Constants.ROLE_NAME, roleName);

                    if (read != null) {
                        List<String> aclRead = Arrays.asList(read);
                        if (aclRead != null) {
                            // Add User to ACL
                            for (String userId : aclRead) {
                                EntityId userEntityId = txn.toEntityId(userId);
                                Entity userOrRoleEntity = txn.getEntity(userEntityId);
                                if (userOrRoleEntity != null) {
                                    entity.addLink(Constants.RESERVED_FIELD_ACL_READ, userOrRoleEntity);
                                    entity.setProperty("read(" + userOrRoleEntity.getId().toString() + ")", true);
                                }
                            }
                        }
                    }

                    entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);

                    if (write != null) {
                        List<String> aclWrite = Arrays.asList(write);
                        if (aclWrite != null) {
                            // Add User to ACL
                            for (String userId : aclWrite) {
                                EntityId userEntityId = txn.toEntityId(userId);
                                Entity userOrRoleEntity = txn.getEntity(userEntityId);
                                if (userOrRoleEntity != null) {
                                    entity.addLink(Constants.RESERVED_FIELD_ACL_WRITE, userOrRoleEntity);
                                    entity.setProperty("write(" + userOrRoleEntity.getId().toString() + ")", true);
                                }
                            }
                        }
                    }

                    entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);


                    entityId[0] = entity.getId().toString();
                }
            });
        } finally {
            ////entityStore.close();
            return entityId[0];
        }
    }

    @Override
    public boolean updateRole(String instance, final String storeName, final String roleId, final String newRoleName,
                              final String[] read, final String[] write, final Boolean publicRead, final Boolean publicWrite) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId roleEntityId = txn.toEntityId(roleId);
                    final Entity entity = txn.getEntity(roleEntityId);
                    entity.setProperty(Constants.ROLE_NAME, newRoleName);

                    if (read != null) {
                        List<String> aclRead = Arrays.asList(read);
                        // Add User to ACL
                        for (String userId : aclRead) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userOrRoleEntity = txn.getEntity(userEntityId);
                            if (userOrRoleEntity != null) {
                                entity.addLink(Constants.RESERVED_FIELD_PUBLICWRITE, userOrRoleEntity);
                                entity.setProperty("read(" + userOrRoleEntity.getId().toString() + ")", true);
                            }
                        }
                    }
                    entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);

                    if (write != null) {
                        List<String> aclWrite = Arrays.asList(write);
                        // Add User to ACL
                        for (String userId : aclWrite) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userOrRoleEntity = txn.getEntity(userEntityId);
                            if (userOrRoleEntity != null) {
                                entity.addLink(Constants.RESERVED_FIELD_ACL_WRITE, userOrRoleEntity);
                                entity.setProperty("write(" + userOrRoleEntity.getId().toString() + ")", true);
                            }
                        }
                    }
                    entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);

                    success[0] = true;
                }
            });
        } finally {
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public Role getRole(String instance, String storeName, final String roleId) {
        final Role[] entity = {null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId roleEntityId = txn.toEntityId(roleId);
                    final Entity roleEntity = txn.getEntity(roleEntityId);
                    Role role = new Role();
                    role.setEntityId(roleEntityId.toString());
                    role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));

                    Boolean publicRead = (Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                    Boolean publicWrite = (Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                    role.setPublicRead(publicRead);
                    role.setPublicWrite(publicWrite);

                    List<EntityStub> aclRead = new LinkedList<>();
                    List<EntityStub> aclWrite = new LinkedList<>();

                    for (Entity aclReadLink : roleEntity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                        aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                    }

                    for (Entity aclWriteLink : roleEntity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                        aclWrite.add(new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                    }

                    role.setAclRead(aclRead);
                    role.setAclWrite(aclWrite);

                    entity[0] = role;
                }
            });
        } finally {
            ////entityStore.close();
        }
        return entity[0];
    }

    @Override
    public boolean deleteRole(String instance, String storeName, final String roleID) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId roleEntityId = txn.toEntityId(roleID);
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
    public boolean linkRole(String instance, String storeName, final String roleID, final String userID) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId userEntityId = txn.toEntityId(userID);
                    EntityId roleEntityId = txn.toEntityId(roleID);
                    Entity userEntity = txn.getEntity(userEntityId);
                    Entity roleEntity = txn.getEntity(roleEntityId);
                    success[0] = userEntity.addLink(Constants.ROLE_NAME, roleEntity);
                }
            });
        } finally {
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean unlinkRole(String instance, String storeName, final String roleID, final String userID) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId userEntityId = txn.toEntityId(userID);
                    EntityId roleEntityId = txn.toEntityId(roleID);
                    Entity userEntity = txn.getEntity(userEntityId);
                    Entity roleEntity = txn.getEntity(roleEntityId);
                    success[0] = userEntity.deleteLink(Constants.ROLE_NAME, roleEntity);
                }
            });
        } finally {
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean isLinked(String instance, String storeName, final String roleID, final String userID) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId userEntityId = txn.toEntityId(userID);
                    EntityId roleEntityId = txn.toEntityId(roleID);
                    Entity userEntity = txn.getEntity(userEntityId);
                    Entity roleEntity = txn.getEntity(roleEntityId);
                    Entity linkedRole = userEntity.getLink(Constants.ROLE_NAME);
                    success[0] = linkedRole.getId().toString().equals(roleEntity.getId().toString());
                }
            });
        } finally {
            ////entityStore.close();
        }
        return success[0];
    }

    @Override
    public List<Role> listRoles(String instance, String storeName, String userIdRoleId, int skip, int limit, String sort, boolean isMasterKey) {
        final List<Role> roles = new LinkedList<>();
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
                        if (sort != null) {
                            if (sort.startsWith("-")) {
                                String sortDescending = sort.substring(1);
                                result = txn.sort(storeName, sortDescending, result, false);
                            } else {
                                String sortAscending = sort.substring(1);
                                result = txn.sort(storeName, sortAscending, result, true);
                            }
                        }
                        long count = result.count();
                        LOG.info("COUNT: " + count);
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
                    for (Entity roleEntity : result) {
                        Role role = new Role();
                        role.setEntityId(roleEntity.getId().toString());
                        role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                        role.setPublicRead((Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD));
                        role.setPublicWrite((Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE));

                        List<EntityStub> aclRead = new LinkedList<>();
                        List<EntityStub> aclWrite = new LinkedList<>();

                        for (Entity aclReadLink : roleEntity.getLinks(Constants.RESERVED_FIELD_ACL_READ)) {
                            aclRead.add(new EntityStub(aclReadLink.getId().toString(), aclReadLink.getType()));
                        }

                        for (Entity aclWriteLink : roleEntity.getLinks(Constants.RESERVED_FIELD_ACL_WRITE)) {
                            aclWrite.add(new EntityStub(aclWriteLink.getId().toString(), aclWriteLink.getType()));
                        }

                        role.setAclRead(aclRead);
                        role.setAclWrite(aclWrite);
                        role.setPublicRead((Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD));
                        role.setPublicWrite((Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE));
                        roles.add(role);
                    }
                }
            });
        } finally {
            ////entityStore.close();
        }
        return roles;
    }

    @Override
    public List<Role> getRolesOfEntity(String instance, final String entityId) {
        final List<Role> roles = new LinkedList<>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId idOfEntity = txn.toEntityId(entityId);
                    final Entity theEntity = txn.getEntity(idOfEntity);
                    for (Entity roleEntity : theEntity.getLinks(Constants.ROLE_LINKNAME)) {
                        Role role = new Role();
                        role.setEntityId(roleEntity.getId().toString());
                        roles.add(role);
                    }
                }
            });
        } finally {
            ////entityStore.close();
        }
        return roles;
    }

}