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

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
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
    public com.divroll.backend.model.File put(String appId, String name, byte[] array) {
        final com.divroll.backend.model.File[] createdFile = {null};
        final Environment env = manager.getEnvironment(xodusRoot, appId);
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
    public com.divroll.backend.model.File put(String appId, String name, InputStream is) {
        final com.divroll.backend.model.File[] createdFile = {null};
        final Environment env = manager.getEnvironment(xodusRoot, appId);
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
    public com.divroll.backend.model.File unmodifiedPut(String appId, String name, InputStream is) {
        throw new IllegalArgumentException("Not yet implemented");
    }

    @Override
    public com.divroll.backend.model.File unmodifiedPut(String appId, String name, byte[] array) {
        throw new IllegalArgumentException("Not yet implemented");
    }

    @Override
    public void get(String appId, String name, OutputStream os) {
        final Environment env = manager.getEnvironment(xodusRoot, appId);
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
    public InputStream getStream(String appId, String name) {
        final InputStream[] input = {null};
        final Environment env = manager.getEnvironment(xodusRoot, appId);
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
    public byte[] get(String appId, String name) {
        final byte[][] targetArray = {null};
        final Environment env = manager.getEnvironment(xodusRoot, appId);
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
    public boolean delete(String appId, String name) {
        final boolean[] success = new boolean[1];
        final Environment env = manager.getEnvironment(xodusRoot, appId);
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
    public boolean isExist(String appId, String name) {
        throw new IllegalArgumentException("Not yet implemented");
    }
}
