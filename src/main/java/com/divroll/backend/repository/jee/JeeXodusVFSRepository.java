/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.repository.jee;

import com.divroll.backend.repository.FileStore;
import com.divroll.backend.xodus.XodusManager;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteSource;
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
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeXodusVFSRepository implements FileStore {

  private static final Logger LOG = LoggerFactory.getLogger(JeeXodusVFSRepository.class);

  @Inject
  @Named("fileStore")
  String fileStore;

  @Inject
  @Named("xodusRoot")
  String xodusRoot;

  @Inject
  XodusManager manager;

  @Override
  public com.divroll.backend.model.File put(
      String appId, String name, byte[] array) {
      final com.divroll.backend.model.File[] createdFile = {null};
      final Environment env = manager.getEnvironment(xodusRoot, appId);
      final VirtualFileSystem vfs  = new VirtualFileSystem(env);
      env.executeInTransaction(
              txn -> {
                  File file = vfs.openFile(txn, name, false);
                  if(file == null) {
                      file = vfs.createFile(txn, name);
                  }
                  try {
                      OutputStream writeStream = vfs.writeFile(txn, file);
                      writeStream.write(array);
                      writeStream.close();
//                      byte[] buff = new byte[64*1024];
//                      InputStream is = ByteSource.wrap(array).openStream();
//                      flow(is, vfs.writeFile(txn, file), buff);
                  } catch (IOException e) {
                      e.printStackTrace();
                  } finally {
                      long fileSize = vfs.getFileLength(txn, file);
                      LOG.info("[File Size: " + fileSize + " File Path: " + file.getPath() +"]");
                      createdFile[0] = new com.divroll.backend.model.File();
                      createdFile[0].setDescriptor(file.getDescriptor());
                      createdFile[0].setName(name);
                      createdFile[0].setCreated(file.getCreated());
                      createdFile[0].setModified(file.getLastModified());
                  }
              });
      vfs.shutdown();
      //env.close();
      return createdFile[0];
  }

  @Override
  public com.divroll.backend.model.File put(
      String appId, String name, InputStream is) {
    final com.divroll.backend.model.File[] createdFile = {null};
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
            txn -> {
//              vfs.deleteFile(txn, name);
              File file = vfs.openFile(txn, name, false);
              if(file == null) {
                  file = vfs.createFile(txn, name);
              }
              try {
                byte[] buff = new byte[64*1024];
                flow(is, vfs.writeFile(txn, file), buff);
              } catch (IOException e) {
                e.printStackTrace();
              } finally {
                long fileSize = vfs.getFileLength(txn, file);
                LOG.info("File Size: " + fileSize + " File Path: " + file.getPath());
                createdFile[0] = new com.divroll.backend.model.File();
                createdFile[0].setDescriptor(file.getDescriptor());
                createdFile[0].setName(name);
                createdFile[0].setCreated(file.getCreated());
                createdFile[0].setModified(file.getLastModified());
              }
            });
    vfs.shutdown();
    //env.close();
    return createdFile[0];
  }

  @Override
  public com.divroll.backend.model.File unmodifiedPut(
      String appId, String name, InputStream is) {
    throw new IllegalArgumentException("Not yet implemented");
  }

  @Override
  public com.divroll.backend.model.File unmodifiedPut(
      String appId, String name, byte[] array) {
    throw new IllegalArgumentException("Not yet implemented");
  }

  @Override
  public void get(String appId, String name, OutputStream os) {
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
        new TransactionalExecutable() {
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
    vfs.shutdown();
    // env.close();
  }

  @Override
  public InputStream getStream(String appId, String name) {
    final InputStream[] input = {null};
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
        new TransactionalExecutable() {
          @Override
          public void execute(@NotNull final Transaction txn) {
            File file = vfs.openFile(txn, name, false);
            if(file != null) {
              input[0] = vfs.readFile(txn, file);
            }
          }
        });
    vfs.shutdown();
    // env.close();
    return input[0];
  }

  @Override
  public byte[] get(String appId, String name) {
    final byte[][] targetArray = {null};
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
        new TransactionalExecutable() {
          @Override
          public void execute(@NotNull final Transaction txn) {
            File file = vfs.openFile(txn, name, false);
            if(file != null) {
                long fileSize = vfs.getFileLength(txn, file);
                InputStream input = vfs.readFile(txn, file);
                LOG.info("File size: " + fileSize);
                try {
                    targetArray[0] = ByteStreams.toByteArray(input);
                    LOG.info("Byte size: " + targetArray[0].length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
          }
        });
    vfs.shutdown();
    // env.close();
    return targetArray[0];
  }

  @Override
  public boolean delete(String appId, String name) {
    final boolean[] success = new boolean[1];
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
        new TransactionalExecutable() {
          @Override
          public void execute(@NotNull final Transaction txn) {
            vfs.deleteFile(txn, name);
            success[0] = true;
          }
        });
    vfs.shutdown();
    // env.close();
    return success[0];
  }

  @Override
  public boolean deleteAll(String appId) {
    final boolean[] success = new boolean[1];
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
            new TransactionalExecutable() {
              @Override
              public void execute(@NotNull final Transaction txn) {
                vfs.getFiles(txn).forEach(file -> {
                  vfs.deleteFile(txn, file.getPath());
                });
                success[0] = true;
              }
            });
    vfs.shutdown();
    // env.close();
    return success[0];
  }

  @Override
  public boolean isExist(String appId, String name) {
    throw new IllegalArgumentException("Not yet implemented");
  }

  @Override
  public boolean move(String appId, String name, String targetName) {
    final boolean[] success = new boolean[1];
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
            new TransactionalExecutable() {
              @Override
              public void execute(@NotNull final Transaction txn) {
                File file = vfs.openFile(txn, name, false);
                vfs.renameFile(txn, file, targetName);
                success[0] = true;
              }
            });
    vfs.shutdown();
    // env.close();
    return success[0];
  }

  @Override
  public List<com.divroll.backend.model.File> list(String appId) {
    List<com.divroll.backend.model.File> files = new LinkedList<>();
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
            new TransactionalExecutable() {
              @Override
              public void execute(@NotNull final Transaction txn) {
                vfs.getFiles(txn).forEach(file -> {
                  String path = file.getPath();
                  long created = file.getCreated();
                  long lastModified = file.getLastModified();
                  long descriptor = file.getDescriptor();
                  com.divroll.backend.model.File newFile = new com.divroll.backend.model.File();
                  newFile.setName(path);
                  newFile.setDescriptor(descriptor);
                  newFile.setCreated(created);
                  newFile.setModified(lastModified);
                  files.add(newFile);
                });
              }
            });
    vfs.shutdown();
    // env.close();
    return files;
  }

  @Override
  public void get(String appId, Long descriptor, OutputStream os) {
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
            new TransactionalExecutable() {
              @Override
              public void execute(@NotNull final Transaction txn) {
                InputStream input = vfs.readFile(txn, descriptor);
                try {
                  ByteStreams.copy(input, os);
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            });
    vfs.shutdown();
    // env.close();
  }

  @Override
  public InputStream getStream(String appId, Long descriptor) {
    final InputStream[] input = {null};
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
            new TransactionalExecutable() {
              @Override
              public void execute(@NotNull final Transaction txn) {
                input[0] = vfs.readFile(txn, descriptor);
              }
            });
    vfs.shutdown();
    // env.close();
    return input[0];
  }

  @Override
  public byte[] get(String appId, Long descriptor) {
    final byte[][] targetArray = {null};
    final Environment env = manager.getEnvironment(xodusRoot, appId);
    final VirtualFileSystem vfs  = new VirtualFileSystem(env);
    env.executeInTransaction(
            new TransactionalExecutable() {
              @Override
              public void execute(@NotNull final Transaction txn) {
                InputStream input = vfs.readFile(txn, descriptor);
                try {
                  targetArray[0] = ByteStreams.toByteArray(input);
                  LOG.info("Byte size: " + targetArray[0].length + "");
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            });
    vfs.shutdown();
    // env.close();
    return targetArray[0];
  }


  public static void flow( InputStream is, OutputStream os, byte[] buf )
          throws IOException {
    int numRead;
    while ( (numRead = is.read(buf) ) >= 0) {
      os.write(buf, 0, numRead);
    }
    if(os != null) {
      os.close();
    }
  }

}
