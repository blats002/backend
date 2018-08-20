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
import com.divroll.domino.model.User;
import com.divroll.domino.repository.UserRepository;
import com.divroll.domino.xodus.XodusManager;
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

    @Inject
    XodusManager manager;

    @Override
    public String createUser(String instance, final String storeName, final String username, final String password,
                             final String[] read, final String[] write, final Boolean publicRead, final Boolean publicWrite, String[] roles) {
        final String[] entityId = {null};

        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);

        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final Entity entity = txn.newEntity(storeName);
                    entity.setProperty(Constants.RESERVED_FIELD_USERNAME, username);
                    entity.setProperty(Constants.RESERVED_FIELD_PASSWORD, password);

                    if (read != null) {
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
                    }

                    entity.setProperty(Constants.RESERVED_FIELD_PUBLICREAD, publicRead);

                    if (write != null) {
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
                    }

                    entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);

                    for(Object roleId : Arrays.asList(roles)) {
                        String id = (String) roleId;
                        EntityId roleEntityId = txn.toEntityId(id);
                        Entity roleEntity = txn.getEntity(roleEntityId);
                        if(roleEntity != null) {
                            entity.addLink(Constants.ROLE_LINKNAME, roleEntity);
                        }
                    }

                    entityId[0] = entity.getId().toString();
                }
            });
        } finally {
            //entityStore.close();
        }
        return entityId[0];
    }

    @Override
    public boolean updateUser(String instance, String storeName, final String userId,
                              final String newUsername, final String newPassword, final String[] read, final String[] write,
                              final Boolean publicRead, final Boolean publicWrite, String[] roles) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
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
                        for (String userId : aclRead) {
                            EntityId userEntityId = txn.toEntityId(userId);
                            Entity userOrRoleEntity = txn.getEntity(userEntityId);
                            if (userOrRoleEntity != null) {
                                entity.addLink(Constants.ACL_READ, userOrRoleEntity);
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
                                entity.addLink(Constants.ACL_WRITE, userOrRoleEntity);
                                entity.setProperty("write(" + userOrRoleEntity.getId().toString() + ")", true);
                            }
                        }
                    }

                    entity.setProperty(Constants.RESERVED_FIELD_PUBLICWRITE, publicWrite);

                    List<String> roleList = Arrays.asList(roles);
                    if(!roleList.isEmpty()) {
                        entity.deleteLinks(Constants.ROLE_LINKNAME);
                }
                    for(String roleId : roleList) {
                    EntityId roleEntityId = txn.toEntityId(roleId);
                    Entity roleEntity = txn.getEntity(roleEntityId);
                    if(roleEntity != null) {
                        entity.addLink(Constants.ROLE_LINKNAME, roleEntity);
                    }
                }

                    success[0] = true;
                }
            });
        } finally {
            //entityStore.close();
        }
        return success[0];
    }

    @Override
    public User getUser(String instance, String storeName, final String userID) {
        final User[] entity = {null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId userEntityId = txn.toEntityId(userID);
                    if(userEntityId != null) {
                        final Entity userEntity = txn.getEntity(userEntityId);
                        User user = new User();
                        user.setUsername((String) userEntity.getProperty(Constants.QUERY_USERNAME));
                        user.setPassword((String) userEntity.getProperty(Constants.QUERY_PASSWORD));
                        user.setEntityId(userEntity.getId().toString());

                        for (Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                            Role role = new Role();
                            role.setEntityId(roleEntity.getId().toString());
                            role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                            role.setPublicRead((Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD));
                            role.setPublicWrite((Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE));

                            List<String> aclRead = new LinkedList<>();
                            List<String> aclWrite = new LinkedList<>();

                            for (Entity aclReadLink : roleEntity.getLinks(Constants.ACL_READ)) {
                                aclRead.add(aclReadLink.getId().toString());
                            }

                            for (Entity aclWriteLink : roleEntity.getLinks(Constants.ACL_WRITE)) {
                                aclWrite.add(aclWriteLink.getId().toString());
                            }

                            role.setAclRead(aclRead);
                            role.setAclWrite(aclWrite);
                            user.getRoles().add(role);
                        }


                        List<String> aclRead = new LinkedList<>();
                        List<String> aclWrite = new LinkedList<>();
                        List<Role> roles = new LinkedList<>();

                        for (Entity aclReadLink : userEntity.getLinks(Constants.ACL_READ)) {
                            aclRead.add(aclReadLink.getId().toString());
                        }

                        for (Entity aclWriteLink : userEntity.getLinks(Constants.ACL_WRITE)) {
                            aclWrite.add(aclWriteLink.getId().toString());
                        }

                        user.setAclRead(aclRead);
                        user.setAclWrite(aclWrite);

                        for(Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                            Role role  = new Role();
                            role.setEntityId(roleEntity.getId().toString());
                            role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                            roles.add(role);
                        }

                        user.setRoles(roles);

                        Boolean publicRead = (Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD);
                        Boolean publicWrite = (Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE);

                        user.setPublicWrite(publicWrite);
                        user.setPublicRead(publicRead);

                        entity[0] = user;
                    }

                }
            });
        } finally {
            //entityStore.close();
        }
        return entity[0];
    }

    @Override
    public User getUserByUsername(String instance, final String storeName, final String username) {
        final User[] entity = {null};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final Entity userEntity = txn.find(storeName, Constants.QUERY_USERNAME, username).getFirst();
                    if (userEntity != null) {
                        User user = new User();
                        user.setUsername((String) userEntity.getProperty(Constants.QUERY_USERNAME));
                        user.setPassword((String) userEntity.getProperty(Constants.QUERY_PASSWORD));
                        user.setEntityId(userEntity.getId().toString());

                        List<Role> roles = new LinkedList<>();
                        for(Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                            Role role  = new Role();
                            role.setEntityId(roleEntity.getId().toString());
                            role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                            roles.add(role);
                        }
                        user.setRoles(roles);

                        entity[0] = user;
                    }
                }
            });
        } finally {
            //entityStore.close();
        }
        return entity[0];
    }

    @Override
    public boolean deleteUser(String instance, String storeName, final String userID) {
        final boolean[] success = {false};
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityId roleEntityId = txn.toEntityId(userID);
                    Entity entity = txn.getEntity(roleEntityId);
                    success[0] = entity.delete();
                }
            });
        } finally {
            //entityStore.close();
        }
        return success[0];
    }

    @Override
    public List<User> listUsers(String instance, final String storeName, final int skip, final int limit) {
        final List<User> users = new LinkedList<>();
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    final EntityIterable allUsers = txn.getAll(storeName).skip(skip).take(limit);
                    for (Entity userEntity : allUsers) {
                        User user = new User();
                        user.setEntityId((String) userEntity.getId().toString());
                        user.setUsername((String) userEntity.getProperty(Constants.QUERY_USERNAME));
                        for (Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                            Role role = new Role();
                            role.setEntityId(roleEntity.getId().toString());
                            role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                            role.setPublicRead((Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD));
                            role.setPublicWrite((Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE));

                            List<String> aclRead = new LinkedList<>();
                            List<String> aclWrite = new LinkedList<>();

                            for (Entity aclReadLink : roleEntity.getLinks(Constants.ACL_READ)) {
                                aclRead.add(aclReadLink.getId().toString());
                            }

                            for (Entity aclWriteLink : roleEntity.getLinks(Constants.ACL_WRITE)) {
                                aclWrite.add(aclWriteLink.getId().toString());
                            }

                            role.setAclRead(aclRead);
                            role.setAclWrite(aclWrite);
                            user.getRoles().add(role);
                        }

                        List<String> aclRead = new LinkedList<>();
                        List<String> aclWrite = new LinkedList<>();

                        for (Entity aclReadLink : userEntity.getLinks(Constants.ACL_READ)) {
                            aclRead.add(aclReadLink.getId().toString());
                        }

                        for (Entity aclWriteLink : userEntity.getLinks(Constants.ACL_WRITE)) {
                            aclWrite.add(aclWriteLink.getId().toString());
                        }

                        user.setAclRead(aclRead);
                        user.setAclWrite(aclWrite);
                        user.setPublicRead((Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD));
                        user.setPublicWrite((Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE));

                        List<Role> roles = new LinkedList<>();
                        for(Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                            Role role  = new Role();
                            role.setEntityId(roleEntity.getId().toString());
                            role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                            roles.add(role);
                        }
                        user.setRoles(roles);

                        users.add(user);
                    }
                }
            });
        } finally {
            //entityStore.close();
        }
        return users;
    }

    @Override
    public List<User> listUsers(String instance, String storeName, String userIdRoleId,
                                int skip, int limit) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, instance);
        final List<User> users = new LinkedList<>();
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull final StoreTransaction txn) {
                    EntityIterable result;
                    if (userIdRoleId == null) {
                        result = txn.find(storeName, "publicRead", true);
                    } else {
                        result = txn.find(storeName, "read(" + userIdRoleId + ")", true)
                            .concat(txn.find(storeName, "publicRead", true));
                    }
                    result = result.skip(skip).take(limit);
                    for (Entity userEntity : result) {
                        User user = new User();
                        user.setEntityId((String) userEntity.getId().toString());
                        user.setUsername((String) userEntity.getProperty(Constants.QUERY_USERNAME));
                        for (Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                            Role role = new Role();
                            role.setEntityId(roleEntity.getId().toString());
                            role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                            role.setPublicRead((Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD));
                            role.setPublicWrite((Boolean) roleEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE));

                            List<String> aclRead = new LinkedList<>();
                            List<String> aclWrite = new LinkedList<>();

                            for (Entity aclReadLink : roleEntity.getLinks(Constants.ACL_READ)) {
                                aclRead.add(aclReadLink.getId().toString());
                            }

                            for (Entity aclWriteLink : roleEntity.getLinks(Constants.ACL_WRITE)) {
                                aclWrite.add(aclWriteLink.getId().toString());
                            }

                            role.setAclRead(aclRead);
                            role.setAclWrite(aclWrite);
                            user.getRoles().add(role);
                        }

                        List<String> aclRead = new LinkedList<>();
                        List<String> aclWrite = new LinkedList<>();

                        for (Entity aclReadLink : userEntity.getLinks(Constants.ACL_READ)) {
                            aclRead.add(aclReadLink.getId().toString());
                        }

                        for (Entity aclWriteLink : userEntity.getLinks(Constants.ACL_WRITE)) {
                            aclWrite.add(aclWriteLink.getId().toString());
                        }

                        user.setAclRead(aclRead);
                        user.setAclWrite(aclWrite);
                        user.setPublicRead((Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_PUBLICREAD));
                        user.setPublicWrite((Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_PUBLICWRITE));

                        List<Role> roles = new LinkedList<>();
                        for(Entity roleEntity : userEntity.getLinks(Constants.ROLE_LINKNAME)) {
                            Role role  = new Role();
                            role.setEntityId(roleEntity.getId().toString());
                            role.setName((String) roleEntity.getProperty(Constants.ROLE_NAME));
                            roles.add(role);
                        }
                        user.setRoles(roles);
                        users.add(user);
                    }
                }
            });

        } finally {

        }
        return users;
    }

}
