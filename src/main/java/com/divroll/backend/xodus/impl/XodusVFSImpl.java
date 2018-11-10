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
        try {
            env.executeInTransaction(new TransactionalExecutable() {
                @Override
                public void execute(@NotNull Transaction txn) {
                    final File file = vfs.createFile(txn, path);
                    final OutputStream output = vfs.writeFile(txn, file);

                }
            });
        } finally {

        }
        return null;
    }

    @Override
    public InputStream openFile(String instance, String path) {
        return null;
    }

    @Override
    public boolean deleteFile(String instance, String path) {
        return false;
    }
}
