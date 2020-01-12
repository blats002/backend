package com.divroll.backend.repository;

import com.divroll.backend.model.File;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface FileRepository {
    File put(String appName, String filePath, byte[] array);
    File put(String appName, String filePath, InputStream is);
    byte[] get(String appName, String filePath);
    InputStream getStream(String appName, String filePath);
    boolean delete(String appName, String filePath);
    boolean deleteAll(String appName);
    boolean move(String appName, String sourceFilePath, String targetFilePath);
    List<File> list(String appName);
}
