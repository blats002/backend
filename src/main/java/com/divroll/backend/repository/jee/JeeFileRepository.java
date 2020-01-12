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
import com.google.common.primitives.Bytes;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    public File put(String appName, String filePath, byte[] array) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appName);
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
    public byte[] get(String appName, String filePath) {
        final PersistentEntityStore entityStore = manager.getPersistentEntityStore(xodusRoot, appName);
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
        // TODO
        return false;
    }

    @Override
    public boolean deleteAll(String appName) {
        return false;
    }

    @Override
    public boolean move(String appName, String sourceFilePath, String targetFilePath) {
        return false;
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
