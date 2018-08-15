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

import com.divroll.domino.model.Role;
import com.divroll.domino.repository.RoleRepository;
import com.divroll.domino.repository.UserRepository;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.*;
import org.jetbrains.annotations.NotNull;
import scala.actors.threadpool.Arrays;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeRoleRepository implements RoleRepository {

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    UserRepository userRepository;

    @Override
    public String createRole(final String instance, final String storeName, final String roleName,
                             final String[] read, final String[] write) {
        final String[] entityId = {null};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final Entity entity = txn.newEntity(storeName);
                    entity.setProperty("name", roleName);

                    boolean publicRead = true;
                    boolean publicWrite = true;

                    if(read != null) {
                        List<String> aclRead = Arrays.asList(read);
                        if(aclRead.contains("*")) {
                            publicRead = true;
                        } else {
                            publicRead = false;
                        }
                        // Add User to ACL
                        for(String userId : aclRead) {
                            if(userId.equals("*")) {
                                continue;
                            } else {
                                EntityId userEntityId = txn.toEntityId(userId);
                                Entity userEntity = txn.getEntity(userEntityId);
                                if(userEntity != null) {
                                    entity.addLink("aclRead", userEntity);
                                }
                            }

                        }
                    }

                    entity.setProperty("publicRead", publicRead);

                    if(write != null) {
                        List<String> aclWrite = Arrays.asList(write);
                        if(aclWrite.contains("*")) {
                            publicWrite = true;
                        } else {
                            publicWrite = false;
                        }
                        // Add User to ACL
                        for(String userId : aclWrite) {
                            if(userId.equals("*")) {
                                continue;
                            } else {
                                EntityId userEntityId = txn.toEntityId(userId);
                                Entity userEntity = txn.getEntity(userEntityId);
                                if(userEntity != null) {
                                    entity.addLink("aclWrite", userEntity);
                                }
                            }

                        }
                    }

                    entity.setProperty("publicWrite", publicWrite);


                    entityId[0] = entity.getId().toString();
                }
            });
        } finally {
            entityStore.close();
            return entityId[0];
        }
    }

    @Override
    public boolean updateRole(String instance, final String storeName, final String roleId, final String newRoleName,
                              final String[] read, final String[] write) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId roleEntityId = txn.toEntityId(roleId);
                    final Entity entity = txn.getEntity(roleEntityId);
                    entity.setProperty("name", newRoleName);

                    if(read != null) {
                        boolean publicRead = true;
                        List<String> aclRead = Arrays.asList(read);
                        if(aclRead.contains("*")) {
                            publicRead = true;
                        } else {
                            publicRead = false;
                        }
                        // Add User to ACL
                        for(String userId : aclRead) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userEntity = txn.getEntity(userEntityId);
                            if(userEntity != null) {
                                entity.addLink("aclRead", userEntity);
                            }
                        }
                        entity.setProperty("publicRead", publicRead);
                    }


                    if(write != null) {
                        boolean publicWrite = true;
                        List<String> aclWrite = Arrays.asList(write);
                        if(aclWrite.contains("*")) {
                            publicWrite = true;
                        } else {
                            publicWrite = false;
                        }
                        // Add User to ACL
                        for(String userId : aclWrite) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userEntity = txn.getEntity(userEntityId);
                            if(userEntity != null) {
                                entity.addLink("aclWrite", userEntity);
                            }
                        }
                        entity.setProperty("publicWrite", publicWrite);
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
    public Role getRole(String instance, String storeName, final String roleId) {
        final Role[] entity = {null};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId roleEntityId = txn.toEntityId(roleId);
                    final Entity roleEntity = txn.getEntity(roleEntityId);
                    Role role = new Role();
                    role.setEntityId(roleEntityId.toString());
                    role.setName((String) roleEntity.getProperty("name"));
                    role.setPublicRead((Boolean) roleEntity.getProperty("publicRead"));
                    role.setPublicWrite((Boolean) roleEntity.getProperty("publicWrite"));

                    List<String> aclRead = new LinkedList<>();
                    List<String> aclWrite = new LinkedList<>();

                    for(Entity aclReadLink : roleEntity.getLinks("aclRead")) {
                        aclRead.add(aclReadLink.getId().toString());
                    }

                    for(Entity aclWriteLink : roleEntity.getLinks("aclWrite")) {
                        aclWrite.add(aclWriteLink.getId().toString());
                    }

                    role.setAclRead(aclRead);
                    role.setAclWrite(aclWrite);

                    entity[0] = role;
                }
            });
        } finally {
            entityStore.close();
        }
        return entity[0];
    }

    @Override
    public boolean deleteRole(String instance, String storeName, final String roleID) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
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
            entityStore.close();
        }

        return success[0];
    }

    @Override
    public boolean linkRole(String instance, String storeName, final String roleID, final String userID) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId userEntityId = txn.toEntityId(userID);
                    EntityId roleEntityId = txn.toEntityId(roleID);
                    Entity userEntity = txn.getEntity(userEntityId);
                    Entity roleEntity = txn.getEntity(roleEntityId);
                    success[0] = userEntity.addLink("role", roleEntity);
                }
            });
        } finally {
            entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean unlinkRole(String instance, String storeName, final String roleID, final String userID) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId userEntityId = txn.toEntityId(userID);
                    EntityId roleEntityId = txn.toEntityId(roleID);
                    Entity userEntity = txn.getEntity(userEntityId);
                    Entity roleEntity = txn.getEntity(roleEntityId);
                    success[0] = userEntity.deleteLink("role", roleEntity);
                }
            });
        } finally {
            entityStore.close();
        }
        return success[0];
    }

    @Override
    public boolean isLinked(String instance, String storeName, final String roleID, final String userID) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId userEntityId = txn.toEntityId(userID);
                    EntityId roleEntityId = txn.toEntityId(roleID);
                    Entity userEntity = txn.getEntity(userEntityId);
                    Entity roleEntity = txn.getEntity(roleEntityId);
                    Entity linkedRole = userEntity.getLink("role");
                    success[0] = linkedRole.getId().toString().equals(roleEntity.getId().toString());
                }
            });
        } finally {
            entityStore.close();
        }
        return success[0];
    }

    @Override
    public List<Role> listRoles(String instance, final String storeName, long skip, long limit) {
        final List<Role> roles = new LinkedList<>();
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final EntityIterable allUsers = txn.getAll(storeName);
                    for (Entity roleEntity : allUsers) {
                        Role role = new Role();
                        role.setEntityId(roleEntity.getId().toString());
                        role.setName((String) roleEntity.getProperty("name"));
                        role.setPublicRead((Boolean) roleEntity.getProperty("publicRead"));
                        role.setPublicWrite((Boolean) roleEntity.getProperty("publicWrite"));
                        roles.add(role);
                    }
                }
            });
        } finally {
            entityStore.close();
        }
        return roles;
    }

}
