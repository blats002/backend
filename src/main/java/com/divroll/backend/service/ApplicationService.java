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
package com.divroll.backend.service;

import com.divroll.backend.model.Application;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.model.filter.TransactionFilter;
import jetbrains.exodus.entitystore.EntityId;

import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface ApplicationService {
  EntityId create(Application application, Superuser superuser);

  Application read(String appId);

  Application readByName(String appName);

  Application readByDomainName(String domainName);

  void update(Application application, String masterKey);

  void delete(String id);

  List<Application> list(List<TransactionFilter> filters, int skip, int limit, Superuser owner);

  void forceUpdate(Application application);

  EntityId attachDomain(String appName, String domainName, Superuser superuser);

  boolean detachDomain(String appName, String domainName, Superuser superuser);

}
