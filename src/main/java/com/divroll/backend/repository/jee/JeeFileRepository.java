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
import com.divroll.backend.model.File;
import com.divroll.backend.repository.FileRepository;
import com.divroll.backend.xodus.XodusManager;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityStore;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class JeeFileRepository extends JeeBaseRespository
    implements FileRepository {

    private static final Logger LOG = LoggerFactory.getLogger(JeeFileRepository.class);


    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    XodusManager manager;

    @Override
    public File put(String appId, String filePath, byte[] array) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appId);
        final File[] file = {null};
        entityStore.executeInTransaction(txn -> {
            if(array.length == 0) {
                throw new IllegalArgumentException("Invalid array size");
            }
            Entity entity = txn.findWithBlob("File", filePath).getFirst();
            try {
                if(entity != null) {
                    entity.setBlob(filePath, ByteSource.wrap(array).openStream());
                    entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
                    file[0] = new File();
                    file[0].setName(filePath);
                    //file.setCreated(entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED));
                } else {
                    entity = txn.newEntity("File");
                    entity.setBlob(filePath, ByteSource.wrap(array).openStream());
                    entity.setProperty(Constants.RESERVED_FIELD_DATE_CREATED, getISODate());
                    entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
                    file[0] = new File();
                    file[0].setName(filePath);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        });
        return file[0];
    }

    @Override
    public File put(String appName, String filePath, InputStream is) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appName);
        final File[] file = {null};
        entityStore.executeInTransaction(txn -> {
            Entity entity = txn.findWithBlob("File", filePath).getFirst();
            try {
                if(entity != null) {
                    byte[] bytes = ByteStreams.toByteArray(is);
                    if(bytes.length == 0) {
                        throw new IllegalArgumentException("Invalid stream size");
                    }
                    CountingInputStream countingInputStream = new CountingInputStream(ByteSource.wrap(bytes).openStream());
                    entity.setBlob(filePath, countingInputStream);
                    entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
                    file[0] = new File();
                    file[0].setName(filePath);
                    //file.setCreated(entity.getProperty(Constants.RESERVED_FIELD_DATE_UPDATED));
                } else {
                    entity = txn.newEntity("File");
                    byte[] bytes = ByteStreams.toByteArray(is);
                    if(bytes.length == 0) {
                        throw new IllegalArgumentException("Invalid stream size");
                    }
                    CountingInputStream countingInputStream = new CountingInputStream(ByteSource.wrap(bytes).openStream());
                    entity.setBlob(filePath, countingInputStream);
                    entity.setProperty(Constants.RESERVED_FIELD_DATE_CREATED, getISODate());
                    entity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
                    file[0] = new File();
                    file[0].setName(filePath);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        });
        return file[0];
    }

    @Override
    public byte[] get(String appId, String filePath) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appId);
        final byte[][] bytes = {null};

        entityStore.executeInTransaction(txn -> {
            Entity entity = txn.findWithBlob("File", filePath).getLast();
            try {
                if(entity != null) {
                    InputStream blobStream = entity.getBlob(filePath);
                    bytes[0] = ByteStreams.toByteArray(blobStream);
                } else {

                }
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        });
        return bytes[0];
    }

    @Override
    public InputStream getStream(String appName, String filePath) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appName);
        final InputStream[] is = {null};
        entityStore.executeInTransaction(txn -> {
            Entity entity = txn.findWithBlob("File", filePath).getFirst();
            try {
                if(entity != null) {
                    InputStream blobStream = entity.getBlob(filePath);
                    is[0] = blobStream;
                } else {

                }
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        });
        return is[0];
    }

    @Override
    public boolean delete(String appName, String filePath) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appName);
        final Boolean[] deleted = {false};
        entityStore.executeInTransaction(txn -> {
            Entity entity = txn.findWithBlob("File", filePath).getFirst();
            if(entity != null) {
                deleted[0] = entity.delete();
            }
        });
        return deleted[0];
    }

    @Override
    public boolean delete(String appName, String fileId, List<String> filePaths) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appName);
        final Boolean[] deleted = {false};
        List<String> mergedFilePaths = new LinkedList<>();

        entityStore.executeInTransaction(txn -> {
            EntityId entityId = txn.toEntityId(fileId);
            if(entityId != null) {
                Entity entity = txn.getEntity(entityId);
                List<String> blobNames = entity.getBlobNames();
                // Get all complete file paths based on folder paths
                for(String blobName : blobNames) {
                    for(String fPath : filePaths) {
                        if(blobName.startsWith(fPath) && !blobName.equals(fPath)) {
                            mergedFilePaths.add(blobName);
                        }
                    }
                }
            }
            mergedFilePaths.addAll(filePaths);
            mergedFilePaths.forEach(filePath -> {
                Entity entity = txn.findWithBlob("File", filePath).getFirst();
                if(entity != null) {
                    entity.delete();
                }
            });
            deleted[0] = true;
        });
        return deleted[0];
    }

    @Override
    public boolean deleteAll(String appName) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appName);
        final boolean[] deleted = {false};
        entityStore.executeInTransaction(txn -> {
            EntityIterable entities = txn.getAll("File");
            final boolean[] hasError = {false};
            entities.forEach(entity -> {
                if (!entity.delete()) {
                    hasError[0] = true;
                }
            });
            deleted[0] = !hasError[0];
        });
        return deleted[0];
    }

    @Override
    public boolean move(String appName, String sourceFilePath, String targetFilePath) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appName);
        final Boolean[] moved = {false};
        entityStore.executeInTransaction(txn -> {
            Entity entity = txn.findWithBlob("File", sourceFilePath).getFirst();
            if(entity != null) {
                InputStream blobStream = entity.getBlob(sourceFilePath);
                entity.setBlob(targetFilePath, blobStream);
                moved[0] = entity.deleteBlob(sourceFilePath);
            }
        });
        return moved[0];
    }

    @Override
    public List<File> list(String appName) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appName);
        List<File> files = new LinkedList<>();
        entityStore.executeInTransaction(txn -> {
            EntityIterable entities = txn.getAll("File");
            entities.forEach(entity -> {
                File file = new File();
                String filePath = entity.getBlobNames().iterator().next();
                file.setName(filePath);
                files.add(file);
            });
        });
        return files;
    }

    @Override
    protected String getDefaultRoleStore() {
        return null;
    }
}
