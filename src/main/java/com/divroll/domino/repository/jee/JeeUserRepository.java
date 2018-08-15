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
import com.divroll.domino.model.User;
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
public class JeeUserRepository implements UserRepository {

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Override
    public String createUser(String instance, final String storeName, final String username, final String password,
                             final String[] read, final String[] write) {
        final String[] entityId = {null};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        entityStore.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
                final Entity entity = txn.newEntity(storeName);
                entity.setProperty("username", username);
                entity.setProperty("password", password);

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
        entityStore.close();
        return entityId[0];
    }

    @Override
    public boolean updateUser(String instance, String storeName, final String userId,
                           final String newUsername, final String newPassword, final String[] read, final String[] write) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        entityStore.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
                EntityId roleEntityId = txn.toEntityId(userId);
                final Entity entity = txn.getEntity(roleEntityId);
                if(newUsername != null) {
                    entity.setProperty("username", newUsername);
                }
                if(newPassword != null) {
                    entity.setProperty("password", newPassword);
                }

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
                        EntityId userEntityId = txn.toEntityId(userId);
                        Entity userEntity = txn.getEntity(userEntityId);
                        if(userEntity != null) {
                            entity.addLink("aclWrite", userEntity);
                        }
                    }
                }

                entity.setProperty("publicWrite", publicWrite);

                success[0] = true;
            }
        });
        entityStore.close();
        return success[0];
    }

    @Override
    public User getUser(String instance, String storeName, final String userID) {
        final User[] entity = {null};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        entityStore.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
                EntityId userEntityId = txn.toEntityId(userID);
                final Entity userEntity = txn.getEntity(userEntityId);
                User user = new User();
                user.setUsername((String) userEntity.getProperty("username"));
                user.setPassword((String) userEntity.getProperty("password"));
                user.setEntityId(userEntity.getId().toString());

                for(Entity roleEntity : userEntity.getLinks("role")) {
                    Role role = new Role();
                    role.setEntityId(roleEntity.getId().toString());
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
                    user.getRoles().add(role);
                }

                entity[0] = user;
            }
        });
        entityStore.close();
        return entity[0];
    }

    @Override
    public User getUserByUsername(String instance, final String storeName, final String username) {
        final User[] entity = {null};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        entityStore.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
                final Entity userEntity = txn.find(storeName, "username", username).getFirst();
                if(userEntity != null) {
                    User user = new User();
                    user.setUsername((String) userEntity.getProperty("username"));
                    user.setPassword((String) userEntity.getProperty("password"));
                    user.setEntityId(userEntity.getId().toString());
                    entity[0] = user;
                }
            }
        });
        entityStore.close();
        return entity[0];
    }

    @Override
    public boolean deleteUser(String instance, String storeName, final String userID) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        entityStore.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull final StoreTransaction txn) {
                EntityId roleEntityId = txn.toEntityId(userID);
                Entity entity = txn.getEntity(roleEntityId);
                success[0] = entity.delete();
            }
        });
        entityStore.close();
        return success[0];
    }

    @Override
    public List<User> listUsers(String instance, final String storeName, long skip, long limit) {
        final List<User> users = new LinkedList<>();
        final PersistentEntityStore entityStore = PersistentEntityStores.newInstance(xodusRoot + instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final EntityIterable allUsers = txn.getAll(storeName);
                    // TODO: How to skip/limit with 100k to millions of records, efficiently
                    for (Entity userEntity : allUsers) {
                        User user = new User();
                        user.setEntityId((String) userEntity.getId().toString());
                        user.setUsername((String) userEntity.getProperty("username"));
                        for(Entity roleEntity : userEntity.getLinks("role")) {
                            Role role = new Role();
                            role.setEntityId(roleEntity.getId().toString());
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
                            user.getRoles().add(role);
                        }
                        users.add(user);
                    }
                }
            });
        } finally {
            entityStore.close();
        }
        return users;
    }

}
