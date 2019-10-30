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
 */
package com.divroll.backend.repository.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.model.exception.DuplicateSuperuserException;
import com.divroll.backend.repository.SuperuserRepository;
import com.divroll.backend.xodus.XodusManager;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.*;
import org.jetbrains.annotations.NotNull;
import org.mindrot.jbcrypt.BCrypt;

import java.util.LinkedList;
import java.util.List;

public class JeeSuperuserRepository extends JeeBaseRespository
        implements SuperuserRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SuperuserRepository.class);

    @Inject
    @Named("defaultSuperuserStore")
    String defaultSuperuserStore;

    @Inject
    @Named("superStore")
    String superStore;

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    XodusManager manager;

    @Override
    public String createUser(String entityType, String username, String password, String email) throws Exception {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, superStore);
        final String[] entityId = {null};
        final Boolean[] isExist = {false};
        try {
            entityStore.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull StoreTransaction txn) {
                    Entity superuser = txn.find(defaultSuperuserStore,
                            Constants.RESERVED_FIELD_USERNAME, username).getFirst();
                    if(superuser == null) {
                        final Entity entity = txn.newEntity(defaultSuperuserStore);
                        entity.setProperty(Constants.RESERVED_FIELD_USERNAME, username);
                        entity.setProperty(Constants.RESERVED_FIELD_PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()));
                        entity.setProperty(Constants.RESERVED_FIELD_DATE_CREATED, getISODate());
                        entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
                        entityId[0] = entity.getId().toString();
                    } else {
                        isExist[0] = true;
                    }
                }
            });
        } finally {

        }
        if(isExist[0]) {
            throw new DuplicateSuperuserException("Username " + username + " already exists");
        }
        return entityId[0];
    }

    @Override
    public boolean updateUser(Superuser superuser) {
        return false;
    }

    @Override
    public boolean updateUserPassword(String userId, String newPassword) {
        return false;
    }

    @Override
    public boolean activateUser(String userId) {
        return false;
    }

    @Override
    public Superuser getUser(String userId) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, superStore);
        final Superuser[] superuser = {null};
        try {
            entityStore.executeInTransaction(txn -> {
                EntityId entityId = txn.toEntityId(userId);
                Entity entity = txn.getEntity(entityId);

                String username = (String) entity.getProperty(Constants.RESERVED_FIELD_USERNAME);
                String password = (String) entity.getProperty(Constants.RESERVED_FIELD_PASSWORD);
                String dateCreated = (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED);
                String dateUpdated = (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED);

                superuser[0] = new Superuser();
                superuser[0].setUsername(username);
                superuser[0].setPassword(password);
                superuser[0].setDateCreated(dateCreated);
                superuser[0].setDateUpdated(dateUpdated);
            });
        } finally {

        }
        return superuser[0];
    }

    @Override
    public Superuser getUserByUsername(String username) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, superStore);
        final Superuser[] superuser = {null};
        try {
            entityStore.executeInTransaction(txn -> {
                Entity entity = txn.find(defaultSuperuserStore,
                        Constants.RESERVED_FIELD_USERNAME, username).getFirst();
                String uname = (String) entity.getProperty(Constants.RESERVED_FIELD_USERNAME);
                String pass = (String) entity.getProperty(Constants.RESERVED_FIELD_PASSWORD);
                String dateCreated = (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_CREATED);
                String dateUpdated = (String) entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED);
                superuser[0] = new Superuser();
                superuser[0].setEntityId(entity.getId().toString());
                superuser[0].setUsername(uname);
                superuser[0].setPassword(pass);
                superuser[0].setDateCreated(dateCreated);
                superuser[0].setDateUpdated(dateUpdated);
            });
        } finally {

        }
        return superuser[0];    }

    @Override
    public boolean deleteUser(String userId) {
        return false;
    }

    @Override
    public List<Superuser> listUsers(int skip, int limit, String sort) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, superStore);
        List<Superuser> superusers = new LinkedList<>();
        try {
            entityStore.executeInTransaction(txn -> {
                EntityIterable result = txn.getAll(defaultSuperuserStore);
                result = result.skip(skip).take(limit);
                if (sort != null) {
                    if (sort.startsWith("-")) {
                        String sortDescending = sort.substring(1);
                        result = txn.sort(defaultSuperuserStore, sortDescending, result, false);
                    } else {
                        String sortAscending = sort.substring(1);
                        result = txn.sort(defaultSuperuserStore, sortAscending, result, true);
                    }
                }
                for (Entity userEntity : result) {
                    Superuser superuser = new Superuser();
                    superuser.setEntityId(userEntity.getId().toString());
                    superuser.setUsername((String) userEntity.getProperty(Constants.RESERVED_FIELD_USERNAME));
                    superuser.setPassword((String) userEntity.getProperty(Constants.RESERVED_FIELD_PASSWORD));
                    superuser.setEmail((String) userEntity.getProperty(Constants.RESERVED_FIELD_EMAIL));
                    superuser.setActive((Boolean) userEntity.getProperty(Constants.RESERVED_FIELD_ACTIVE));
                    superusers.add(superuser);
                }
            });
        } finally {

        }
        return superusers;
    }

    @Override
    protected String getDefaultRoleStore() {
        return null;
    }
}
