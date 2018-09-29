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
import com.divroll.backend.model.filter.TransactionFilter;

import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface UserRepository {
    String createUser(String instance, String storeName, String username, String password,
                      String[] read, String[] write,
                      final Boolean publicRead, final Boolean publicWrite, String[] roles);

    boolean updateUser(String instance, String storeName, String entityId,
                       String newUsername, String newPassword,
                       String[] read, String[] write,
                       final Boolean publicRead, final Boolean publicWrite, String[] roles);

    boolean updateUserPassword(String instance, String storeName, String entityId, String newPassword);

    User getUser(String instance, String storeName, String userID);

    User getUserByUsername(String instance, String storeName, String username);

    boolean deleteUser(String instance, String storeName, String userID);

    List<User> listUsers(String instance, String storeName, String userIdRoleId,
                         int skip, int limit, String sort, boolean isMasterkey, List<TransactionFilter> filters);
}
