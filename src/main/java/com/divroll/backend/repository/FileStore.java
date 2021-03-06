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
import java.io.OutputStream;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public interface FileStore {
  File put(String appId, String name, byte[] array);

  File put(String appId, String name, InputStream is);

  File unmodifiedPut(String appId, String name, InputStream is);

  File unmodifiedPut(String appId, String name, byte[] array);

  void get(String appId, String name, OutputStream os);

  InputStream getStream(String appId, String name);

  byte[] get(String appId, String name);

  boolean delete(String appId, String name);

  boolean deleteAll(String appId);

  boolean isExist(String appId, String name);

  boolean move(String appId, String name, String targetName);

  List<File> list(String appId);

  void get(String appId, Long descriptor, OutputStream os);

  InputStream getStream(String appId, Long descriptor);

  byte[] get(String appId, Long descriptor);

}
