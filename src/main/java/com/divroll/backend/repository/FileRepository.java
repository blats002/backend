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
package com.divroll.backend.repository;

import com.divroll.backend.model.File;

import java.io.InputStream;
import java.util.List;

public interface FileRepository {
    File put(String appId, String filePath, byte[] array);
    File put(String appId, String filePath, InputStream is);
    byte[] get(String appId, String filePath);
    InputStream getStream(String appId, String filePath);
    boolean delete(String appId, String filePath);
    boolean delete(String appId, String fileId, List<String> filePaths);
    boolean deleteAll(String appId);
    boolean move(String appId, String sourceFilePath, String targetFilePath);
    List<File> list(String appName);
}
