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

import com.divroll.backend.model.action.Action;
import com.divroll.backend.model.action.EntityAction;
import com.divroll.backend.model.builder.CreateOption;
import com.divroll.backend.model.builder.EntityClass;
import com.divroll.backend.model.builder.EntityMetadata;
import com.divroll.backend.model.filter.TransactionFilter;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface EntityRepository {

  String createEntity(
      String instance,
      String namespace,
      String entityType,
      EntityClass entityClass,
      List<Action> actions,
      List<EntityAction> entityActions,
      CreateOption createOption,
      final EntityMetadata metadata);

  boolean updateEntity(
      String instance,
      String namespace,
      String entityType,
      String entityId,
      Map<String, Comparable> comparableMap,
      final String[] read,
      final String[] write,
      final Boolean publicRead,
      final Boolean publicWrite,
      final EntityMetadata metadata);

  <T> Map<String, Comparable> getFirstEntity(
      String dir,
      String namespace,
      final String kind,
      final String propertyKey,
      final Comparable<T> propertyVal,
      Class<T> clazz);

  <T> InputStream getFirstEntityBlob(
      String dir,
      String namespace,
      final String kind,
      final String propertyKey,
      final Comparable<T> propertyVal,
      Class<T> clazz,
      String blobKey);

  Map<String, Comparable> getEntity(
      String instance, String namespace, String entityType, String entityId, List<String> includes);

  List<Map<String, Comparable>> getEntity(
          String instance, String namespace, String entityType, String propertyName, Comparable propertyValue, List<String> includes);

  List<String> getACLReadList(String instance, String namespace, String entityId);

  List<String> getACLWriteList(String instance, String namespace, String entityId);

  boolean isPublicRead(String instance, String namespace, String entityId);

  boolean isPublicWrite(String instance, String namespace, String entityId);

  Comparable getEntityProperty(
      String instance, String namespace, String entityType, String entityId, String propertyName);

  boolean deleteEntity(String instance, String namespace, String entityType, String entityId);

  boolean deleteEntities(String instance, String namespace, String entityType);

  boolean deleteEntities(String instance, String namespace, String entityType, String propertyName, Comparable propertyValue);

  boolean deleteEntityType(String instance, String namespace, String entityType);

  boolean linkEntity(
      String instance,
      String namespace,
      String entityType,
      String linkName,
      String sourceId,
      String targetId,
      boolean bSet);

  boolean unlinkEntity(
      String instance,
      String namespace,
      String entityType,
      String linkName,
      String sourceId,
      String targetId);

  boolean isLinked(
      String instance,
      String namespace,
      String entityType,
      String linkName,
      String sourceId,
      String targetId);

  Map<String, Comparable> getFirstLinkedEntity(
      String instance, String namespace, String entityType, String entityId, String linkName);

  List<Map<String, Comparable>> getLinkedEntities(
      String instance, String namespace, String entityType, String entityId, String linkName);

  List<Map<String, Comparable>> getLinkedEntitiesByEntityType(
          String instance,
          String namespace,
          String entityType,
          final String linkName);

  List<Map<String, Comparable>> listEntities(
      String instance,
      String namespace,
      String entityType,
      String userIdRoleId,
      int skip,
      int limit,
      String sort,
      String linkName,
      String linkedTo,
      boolean isMasterKey,
      List<TransactionFilter> filters);

  Long countEntities(
          String instance,
          String namespace,
          String entityType,
          boolean isMasterKey,
          List<TransactionFilter> filters);

  InputStream getEntityBlob(
      String instance, String namespace, String entityType, String entityId, String blobKey);

  Boolean replaceBlobName(String instance, String namespace, String entityType, String entityId, String pattern, String replacement);

  Long countEntityBlobSize(String instance, String namespace, String entityType, String entityId, String blobKey);

  boolean createEntityBlob(
      String instance,
      String namespace,
      String entityType,
      String entityId,
      String blobKey,
      InputStream is);

  boolean createEntityBlob(
          String instance,
          String namespace,
          String entityType,
          String propertyName,
          Comparable propertyValue,
          String blobKey,
          InputStream is);

  boolean deleteEntityBlob(
      String instance, String namespace, String entityType, String entityId, String blobKey);

  List<String> getLinkNames(String instance, String namespace, String entityType, String entityId);

  List<String> getBlobKeys(String instance, String namespace, String entityType, String entityId);

  boolean deleteProperty(String instance, String namespace, String entityType, String propertyName);

  boolean updateProperty(
      String instance,
      String namespace,
      String entityType,
      String propertyName,
      EntityMetadata update);

    boolean updateProperty(
            String instance,
            String namespace,
            String entityType,
            String entityId,
            String propertyName,
            Comparable propertyValue,
            EntityMetadata update);

  List<Map<String, Comparable>> getEntities(
      String instance,
      String namespace,
      String entityType,
      String propertyName,
      Comparable propertyValue,
      int skip,
      int limit, List<String> includes);
}
