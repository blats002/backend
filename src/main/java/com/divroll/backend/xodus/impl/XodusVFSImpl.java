package com.divroll.backend.xodus.impl;

import com.divroll.backend.xodus.XodusManager;
import com.divroll.backend.xodus.XodusVFS;
import com.google.inject.Inject;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalExecutable;
import jetbrains.exodus.vfs.File;
import jetbrains.exodus.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;

import javax.inject.Named;
import java.io.InputStream;
import java.io.OutputStream;

public class XodusVFSImpl implements XodusVFS {

    @Inject
    XodusManager manager;

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Override
    public OutputStream createFile(String instance, String path, InputStream stream) {
        Environment env = manager.getEnvironment(xodusRoot, instance);
        final VirtualFileSystem vfs = new VirtualFileSystem(env);
        final OutputStream[] output = new OutputStream[1];
        try {
            env.executeInTransaction(new TransactionalExecutable() {
                @Override
                public void execute(@NotNull Transaction txn) {
                    final File file = vfs.createFile(txn, path);
                    output[0] = vfs.writeFile(txn, file);
                }
            });
        } finally {

        }
        return output[0];
    }

    @Override
    public InputStream openFile(String instance, String path) {
        Environment env = manager.getEnvironment(xodusRoot, instance);
        final VirtualFileSystem vfs = new VirtualFileSystem(env);
        final InputStream[] inputStream = new InputStream[1];
        try {
            env.executeInTransaction(new TransactionalExecutable() {
                @Override
                public void execute(@NotNull Transaction txn) {
                    File file = vfs.openFile(txn, path, false);
                    inputStream[0] = vfs.readFile(txn, file);
                }
            });
        } finally {
            // Do nothing
        }
        return inputStream[0];
    }

    @Override
    public boolean deleteFile(String instance, String path) {
        Environment env = manager.getEnvironment(xodusRoot, instance);
        final VirtualFileSystem vfs = new VirtualFileSystem(env);
        final Boolean[] success = {false};
        try {
            env.executeInTransaction(new TransactionalExecutable() {
                @Override
                public void execute(@NotNull Transaction txn) {
                    File file = vfs.deleteFile(txn, path);
                    success[0] = true;
                }
            });
        } finally {
            // Do nothing
        }
        return success[0];
    }

}
