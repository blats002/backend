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
