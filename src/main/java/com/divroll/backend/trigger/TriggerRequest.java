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
package com.divroll.backend.trigger;

import com.divroll.backend.repository.jee.AppEntityRepository;
import com.divroll.backend.service.jee.AppEmailService;

import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class TriggerRequest {

  private String entityType;
  private Map<String, Comparable> entity;
  private AppEntityRepository query;
  private AppEmailService email;

  private TriggerRequest() {}

  public TriggerRequest(
      Map<String, Comparable> entity,
      String entityType,
      AppEntityRepository query,
      AppEmailService email) {
    setEntity(entity);
    setQuery(query);
    setEntityType(entityType);
    setEmail(email);
  }

  public Map<String, Comparable> getEntity() {
    return entity;
  }

  public void setEntity(Map<String, Comparable> entity) {
    this.entity = entity;
  }

  public AppEntityRepository getQuery() {
    return query;
  }

  public void setQuery(AppEntityRepository query) {
    this.query = query;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public AppEmailService getEmail() {
    return email;
  }

  public void setEmail(AppEmailService email) {
    this.email = email;
  }
}
