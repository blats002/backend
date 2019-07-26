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
