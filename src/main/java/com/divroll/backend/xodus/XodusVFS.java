package com.divroll.backend.xodus;

import java.io.InputStream;
import java.io.OutputStream;

public interface XodusVFS {
    OutputStream createFile(String instance, String path, InputStream stream);
    InputStream openFile(String instance, String path);
    boolean deleteFile(String instance, String path);
}
