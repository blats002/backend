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
