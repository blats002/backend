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

import com.divroll.backend.model.EntityPropertyType;
import com.divroll.backend.model.filter.TransactionFilter;
import jetbrains.exodus.entitystore.EntityId;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface XodusStore {

  EntityId putIfNotExists(
      String dir,
      String namespace,
      final String kind,
      Map<String, Comparable> properties,
      String uniqueProperty);

  EntityId putIfNotExists(
          String dir,
          String namespace,
          final String kind,
          Map<String, Comparable> properties,
          Map<String, String> links,
          Map<String, List<String>> multiLinks,
          String uniqueProperty);

  EntityId put(
      String dir, String namespace, final String kind, Map<String, Comparable> properties);

  <T> EntityId put(
      String dir, String namespace, String kind, String id, Map<String, Comparable> comparableMap);

  Map<String, Comparable> get(String dir, String namespace, String id);

  Map<String, Comparable> get(String dir, String namespace, EntityId id);

  <T> T get(String dir, String namespace, String kind, String id, String key);

  byte[] getBlob(String dir, String namespace, final String kind, final String blobKey);

  EntityId update(
      String dir,
      String namespace,
      final String kind,
      String id,
      Map<String, Comparable> properties);

  boolean delete(String dir, String namespace, final String id);

  boolean delete(String dir, String namespace, final String... id);

  boolean delete(
      String dir, String namespace, String kind, String propertyName, Comparable propertyValue);

  <T> EntityId getFirstEntityId(
      String dir,
      String namespace,
      final String kind,
      final String propertyKey,
      Comparable<T> propertyVal,
      Class<T> clazz);

  List<Map<String, Comparable>> list(
      String dir, String namespace, final String entityType, int skip, int limit,
      Map<String,String> links, Map<String,List<String>> multiLinks);

  List<Map<String, Comparable>> list(
      String dir,
      String namespace,
      final String entityType,
      List<TransactionFilter> filters,
      int skip,
      int limit,
      Map<String,String> links, Map<String,List<String>> multiLinks);

  List<EntityPropertyType> listPropertyTypes(
      final String dir, String namespace, final String entityType);

  List<String> listEntityTypes(String dir, String namespace);
}
