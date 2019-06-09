/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019, Divroll, and individual contributors
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
