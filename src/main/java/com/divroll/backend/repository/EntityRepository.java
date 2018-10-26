/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
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

import com.divroll.backend.model.action.Action;
import com.divroll.backend.model.builder.EntityClass;
import com.divroll.backend.model.filter.TransactionFilter;
import jetbrains.exodus.entitystore.EntityId;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface EntityRepository {
    String createEntity(String instance, String storeName, EntityClass entityClass,
                        List<Action> actions, final List<String> uniqueProperties);

    boolean updateEntity(String instance, String storeName, String entityId, Map<String, Comparable> comparableMap,
                         final String[] read, final String[] write, final Boolean publicRead, final Boolean publicWrite,
                         final List<String> uniqueProperties);

    <T> Map<String, Object> getFirstEntity(String dir, final String kind, final String propertyKey, final Comparable<T> propertyVal, Class<T> clazz);

    <T> InputStream getFirstEntityBlob(String dir, final String kind, final
            String propertyKey, final Comparable<T> propertyVal, Class<T> clazz,
                                       String blobKey);

    Map<String, Object> getEntity(String instance, String storeName, String entityId);

    Comparable getEntityProperty(String instance, String storeName, String entityId, String propertyName);

    boolean deleteEntity(String instance, String storeName, String entityId);

    boolean deleteEntities(String instance, String storeName);

    boolean deleteEntityType(String instance, String entityType);

    boolean linkEntity(String instance, String storeName, String linkName, String sourceId, String targetId);

    boolean unlinkEntity(String instance, String storeName, String linkName, String sourceId, String targetId);

    boolean isLinked(String instance, String storeName, String linkName, String sourceId, String targetId);

    Map<String, Object> getFirstLinkedEntity(String instance, String storeName, String entityId, String linkName);

    List<Map<String, Object>> getLinkedEntities(String instance, String storeName, String entityId, String linkName);

    List<Map<String, Object>> listEntities(String instance, String storeName, String userIdRoleId,
                                           int skip, int limit, String sort, boolean isMasterKey, List<TransactionFilter> filters);

    InputStream getEntityBlob(String instance, String storeName, String entityId, String blobKey);

    boolean createEntityBlob(String instance, String storeName, String entityId, String blobKey, InputStream is);

    boolean deleteEntityBlob(String instance, String storeName, String entityId, String blobKey);

    List<String> getLinkNames(String instance, String storeName, String entityId);

    List<String> getBlobKeys(String instance, String storeName, String entityId);

    boolean deleteProperty(String instance, String storeName, String propertyName);

    List<Map<String, Object>> getEntities(String instance, String storeName, String propertyName, Comparable propertyValue, int skip, int limit);

}
