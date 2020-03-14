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
package com.divroll.backend.helper;

import com.divroll.backend.model.Role;
import com.divroll.backend.model.User;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class DTOHelper {
  public static String[] roleIdsOnly(List<Role> roleDTOS) {
    if (roleDTOS == null) {
      return null;
    }
    String[] roleArray = new String[roleDTOS.size()];
    int idx = 0;
    for (Role roleDTO : roleDTOS) {
      roleArray[idx] = roleDTO.getEntityId();
      idx++;
    }
    return roleArray;
  }

  public static List<User> convert(List<User> users) {
    List<User> userDTOS = null;
    if (users == null) {
      return null;
    }
    for (User user : users) {
      if (userDTOS == null) {
        userDTOS = new LinkedList<User>();
      }
      userDTOS.add(user);
    }
    return userDTOS;
  }
}
