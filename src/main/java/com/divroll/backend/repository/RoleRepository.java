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

import com.divroll.backend.model.Role;
import com.divroll.backend.model.action.Action;
import com.divroll.backend.model.filter.TransactionFilter;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface RoleRepository {
  String createRole(
      String instance,
      String namespace,
      String entityType,
      String roleName,
      String[] read,
      String[] write,
      final Boolean publicRead,
      final Boolean publicWrite,
      List<Action> actions);

  boolean updateRole(
      String instance,
      String namespace,
      String entityType,
      String entityId,
      String newRoleName,
      final String[] read,
      final String[] write,
      final Boolean publicRead,
      final Boolean publicWrite);

  boolean updateRole(
      String instance,
      String namespace,
      String entityType,
      String entityId,
      Map<String, Comparable> comparableMap,
      final String[] read,
      final String[] write,
      final Boolean publicRead,
      final Boolean publicWrite);

  Role getRole(String instance, String namespace, String entityType, String entityId);

  boolean deleteRole(String instance, String namespace, String entityType, String roleID);

  boolean linkRole(
      String instance, String namespace, String entityType, String roleID, String userID);

  boolean unlinkRole(
      String instance, String namespace, String entityType, String roleID, String userID);

  boolean isLinked(
      String instance, String namespace, String entityType, String roleID, String userID);

  List<Role> listRoles(
      String instance,
      String namespace,
      String entityType,
      String userIdRoleId,
      int skip,
      int limit,
      String sort,
      boolean isMasterKey,
      List<TransactionFilter> filters);

  List<Role> getRolesOfEntity(String instance, String namespace, String entityId);
}
