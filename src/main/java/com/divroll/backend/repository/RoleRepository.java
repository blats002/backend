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
