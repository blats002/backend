/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
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
