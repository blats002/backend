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

  User getUser(String instance, String namespace, String entityType, String userID, List<String> includeLinks);

  User getUserByUsername(String instance, String namespace, String entityType, String username, List<String> includeLinks);

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
      List<TransactionFilter> filters, List<String> includeLinks);
}
