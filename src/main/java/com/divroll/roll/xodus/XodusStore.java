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
package com.divroll.roll.xodus;

import jetbrains.exodus.entitystore.EntityId;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface XodusStore {

    //    public void putIfNotExists(String dir, final String entityType, final String propertyKey, final String propertyValue);
//    public void putIfNotExists(String dir, final String entityType, final String propertyKey, final Double propertyValue);
//    public void putIfNotExists(String dir, final String entityType, final String propertyKey, final Long propertyValue);
//    public void putIfNotExists(String dir, final String entityType, final String propertyKey, final Boolean propertyValue);
//    public EntityId putIfNotExists(String dir, final String entityType, final String propertyKey, final InputStream is);
    public EntityId put(String dir, final String kind, Map<String, Comparable> properties);

    public <T> EntityId put(String dir, String kind, String id, Map<String, Comparable> comparableMap);

    public Map<String, Comparable> get(String dir, String id);

    public <T> T get(String dir, String kind, String id, String key);

    public byte[] getBlob(String dir, final String kind, final String blobKey);

    public EntityId update(String dir, final String kind, String id, Map<String, Comparable> properties);

    public void delete(String dir, final String id);

    public void delete(String dir, final String... id);

    public <T> EntityId getFirstEntityId(String dir, final String kind, final String propertyKey,
                                         Comparable<T> propertyVal, Class<T> clazz);

    List<Map<String, Comparable>> list(String dir, final String entityType, int skip, int limit);

}
