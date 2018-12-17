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

import com.divroll.backend.model.User;
import com.divroll.backend.model.action.Action;
import com.divroll.backend.model.builder.EntityClass;
import com.divroll.backend.model.filter.TransactionFilter;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface UserRepository {
  String createUser(
      String instance,
      String namespace,
      String entityType,
      String username,
      String password,
      final Map<String, Comparable> comparableMap,
      String[] read,
      String[] write,
      final Boolean publicRead,
      final Boolean publicWrite,
      String[] roles,
      List<Action> actions,
      EntityClass linkedEntity,
      String linkName,
      String backlinkName);

  boolean updateUser(
      String instance,
      String namespace,
      String entityType,
      String entityId,
      String newUsername,
      String newPassword,
      final Map<String, Comparable> comparableMap,
      String[] read,
      String[] write,
      final Boolean publicRead,
      final Boolean publicWrite,
      String[] roles);

  boolean updateUserPassword(
      String instance, String namespace, String entityType, String entityId, String newPassword);

  boolean updateUser(
      String instance,
      String namespace,
      String entityType,
      String entityId,
      Map<String, Comparable> comparableMap,
      final String[] read,
      final String[] write,
      final Boolean publicRead,
      final Boolean publicWrite);

  User getUser(String instance, String namespace, String entityType, String userID);

  User getUserByUsername(String instance, String namespace, String entityType, String username);

  boolean deleteUser(String instance, String namespace, String entityType, String userID);

  List<User> listUsers(
      String instance,
      String namespace,
      String entityType,
      String userIdRoleId,
      int skip,
      int limit,
      String sort,
      boolean isMasterkey,
      List<String> roleNames,
      List<TransactionFilter> filters);
}
