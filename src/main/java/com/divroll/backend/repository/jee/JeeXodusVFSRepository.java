package com.divroll.backend.repository.jee;

import com.divroll.backend.repository.FileStore;
import com.divroll.backend.xodus.XodusManager;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalExecutable;
import jetbrains.exodus.vfs.File;
import jetbrains.exodus.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class JeeXodusVFSRepository implements FileStore {

    @Inject
    @Named("fileStore")
    String fileStore;

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    XodusManager manager;

    @Override
    public com.divroll.backend.model.File put(String name, byte[] array) {
        final com.divroll.backend.model.File[] createdFile = {null};
        final Environment env = manager.getEnvironment(xodusRoot, fileStore);
        final VirtualFileSystem vfs = manager.getVirtualFileSystem(env);
        env.executeInTransaction(new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
                String filePrefix = UUID.randomUUID().toString().replace("-", "");
                String fileName = filePrefix + "-" + name;
                final File file = vfs.createFile(txn, fileName);
                try (DataOutputStream output = new DataOutputStream(vfs.writeFile(txn, file))) {
                    output.write(array);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    createdFile[0] = new com.divroll.backend.model.File();
                    createdFile[0].setName(fileName);
                }
            }
        });
        //vfs.shutdown();
        //env.close();
        return createdFile[0];
    }

    @Override
    public com.divroll.backend.model.File put(String name, InputStream is) {
        final com.divroll.backend.model.File[] createdFile = {null};
        final Environment env = manager.getEnvironment(xodusRoot, fileStore);
        final VirtualFileSystem vfs = manager.getVirtualFileSystem(env);
        env.executeInTransaction(new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
                String filePrefix = UUID.randomUUID().toString().replace("-", "");
                String fileName = filePrefix + "-" + name;
                final File file = vfs.createFile(txn, fileName);
                try (DataOutputStream output = new DataOutputStream(vfs.writeFile(txn, file))) {
                    output.write(ByteStreams.toByteArray(is));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    createdFile[0] = new com.divroll.backend.model.File();
                    createdFile[0].setName(fileName);
                }
            }
        });
        //vfs.shutdown();
        //env.close();
        return createdFile[0];    }

    @Override
    public com.divroll.backend.model.File unmodifiedPut(String name, InputStream is) {
        throw new IllegalArgumentException("Not yet implemented");
    }

    @Override
    public com.divroll.backend.model.File unmodifiedPut(String name, byte[] array) {
        throw new IllegalArgumentException("Not yet implemented");
    }

    @Override
    public void get(String name, OutputStream os) {
        final Environment env = manager.getEnvironment(xodusRoot, fileStore);
        final VirtualFileSystem vfs = manager.getVirtualFileSystem(env);
        env.executeInTransaction(new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
                File file = vfs.createFile(txn, name);
                InputStream input = vfs.readFile(txn, file);
                try {
                    ByteStreams.copy(input, os);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //vfs.shutdown();
        //env.close();
    }

    @Override
    public InputStream getStream(String name) {
        final InputStream[] input = {null};
        final Environment env = manager.getEnvironment(xodusRoot, fileStore);
        final VirtualFileSystem vfs = manager.getVirtualFileSystem(env);
        env.executeInTransaction(new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
                File file = vfs.openFile(txn, name, false);
                input[0] = vfs.readFile(txn, file);
            }
        });
        vfs.shutdown();
        //env.close();
        return input[0];
    }

    @Override
    public byte[] get(String name) {
        final byte[][] targetArray = {null};
        final Environment env = manager.getEnvironment(xodusRoot, fileStore);
        final VirtualFileSystem vfs = manager.getVirtualFileSystem(env);
        env.executeInTransaction(new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
                File file = vfs.openFile(txn, name, false);
                InputStream input = vfs.readFile(txn, file);
                try {
                    targetArray[0] = ByteStreams.toByteArray(input);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //vfs.shutdown();
        //env.close();
        return targetArray[0];
    }

    @Override
    public boolean delete(String name) {
        final boolean[] success = new boolean[1];
        final Environment env = manager.getEnvironment(xodusRoot, fileStore);
        final VirtualFileSystem vfs = manager.getVirtualFileSystem(env);
        env.executeInTransaction(new TransactionalExecutable() {
            @Override
            public void execute(@NotNull final Transaction txn) {
                vfs.deleteFile(txn, name);
                success[0] = true;
            }
        });
        //vfs.shutdown();
        //env.close();
        return success[0];
    }

    @Override
    public boolean isExist(String name) {
        throw new IllegalArgumentException("Not yet implemented");
    }
}
