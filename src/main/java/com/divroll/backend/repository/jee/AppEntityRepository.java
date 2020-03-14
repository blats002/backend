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
package com.divroll.backend.repository.jee;

import com.divroll.backend.repository.EntityRepository;

import java.util.LinkedList;
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
    return repository.getEntity(instance, namespace, entityType, entityId, new LinkedList<>());
  }

  public boolean isExist(String entityType, String propertyName, Comparable propertyValue) {
    List<Map<String, Comparable>> entities =
        repository.getEntities(instance, namespace, entityType, propertyName, propertyValue, 0, 1, new LinkedList<>());
    return !entities.isEmpty();
  }
}
