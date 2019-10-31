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
package com.divroll.backend.repository;

import com.divroll.backend.model.Superuser;
import com.divroll.backend.model.exception.DuplicateSuperuserException;

import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface SuperuserRepository {
  String createUser(
          String entityType,
          String username,
          String password,
          String email) throws Exception;

  boolean updateUser(Superuser superuser);

  boolean updateUserPassword(String userId, String newPassword);

  boolean activateUser(String email);

  Superuser getUser(String userId);

  Superuser getUserByUsername(String username);

  Superuser getUserByEmail(String email);

  boolean deleteUser(String userId);

  List<Superuser> listUsers(
          int skip,
          int limit,
          String sort);
}
