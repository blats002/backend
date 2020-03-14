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
package com.divroll.backend.xodus;

import com.divroll.backend.model.ByteValue;

import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface XodusEnvStore {
  public void put(String instance, String store, String key, String value);

  public void put(String instance, String store, String key, Boolean value);

  public void put(String instance, String store, String key, Double value);

  public void put(String instance, String store, String key, Float value);

  public void put(String instance, String store, String key, Integer value);

  public void put(String instance, String store, String key, Long value);

  public void put(String instance, String store, String key, Short value);

  public boolean put(String instance, String store, String key, ByteValue value);

  public boolean putIfNotExists(String instance, String store, String key, ByteValue value);

  public boolean batchPut(
      String instance, final String entityType, final Map<String, String> properties);

  public <T> T get(String instance, String store, String key, final Class<T> clazz);

  public boolean delete(String instance, String store, String key);

  public boolean delete(String instance, String store, String... keys);

  public boolean batchPutDelete(
      String instance,
      final String entityType,
      final Map<String, String> properties,
      final String... keys);
}
