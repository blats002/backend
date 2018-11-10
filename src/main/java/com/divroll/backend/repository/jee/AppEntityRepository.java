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
package com.divroll.backend.repository.jee;

import com.divroll.backend.repository.EntityRepository;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class AppEntityRepository {

  private final EntityRepository repository;
  private final String instance;
  private final String namespace;
  private final String entityType;

  public AppEntityRepository(
      EntityRepository repository, String instance, String namespace, String entityType) {
    this.repository = repository;
    this.entityType = entityType;
    this.instance = instance;
    this.namespace = namespace;
  }

  public Map<String, Comparable> getEntityById(String namespace, String entityId) {
    return repository.getEntity(instance, namespace, entityType, entityId);
  }

  public boolean isExist(String entityType, String propertyName, Comparable propertyValue) {
    List<Map<String, Comparable>> entities =
        repository.getEntities(instance, namespace, entityType, propertyName, propertyValue, 0, 1);
    return !entities.isEmpty();
  }
}
