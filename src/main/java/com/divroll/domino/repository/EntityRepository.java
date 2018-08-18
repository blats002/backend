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
package com.divroll.domino.repository;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface EntityRepository {
    String createEntity(String instance, String storeName, Map<String, Comparable> comparableMap,
                        final String[] read, final String[] write, final Boolean publicRead, final Boolean publicWrite);

    boolean updateEntity(String instance, String storeName, String entityId, Map<String, Comparable> comparableMap,
                         final String[] read, final String[] write, final Boolean publicRead, final Boolean publicWrite);

    Map<String, Object> getEntity(String instance, String storeName, String entityId);

    List<Map<String, Object>> listEntities(String instance, String storeName, int skip, int limit);

    Comparable getEntityProperty(String instance, String storeName, String entityId, String propertyName);

    InputStream getEntityBlob(String instance, String storeName, String entityId, String blobKey);

    boolean deleteEntity(String instance, String storeName, String entityId);

    boolean linkEntity(String instance, String storeName, String linkName, String sourceId, String targetId);

    boolean unlinkEntity(String instance, String storeName, String linkName, String sourceId, String targetId);

    boolean isLinked(String instance, String storeName, String linkName, String sourceId, String targetId);

    Map<String, Object> getFirstLinkedEntity(String instance, String storeName, String entityId, String linkName);

    List<Map<String, Object>> getLinkedEntities(String instance, String storeName, String entityId, String linkName);
}
