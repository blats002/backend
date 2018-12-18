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
package com.divroll.backend.helper;

import com.divroll.backend.model.Role;
import com.divroll.backend.model.RoleDTO;
import com.divroll.backend.model.User;
import com.divroll.backend.model.UserDTO;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class DTOHelper {
  public static String[] roleIdsOnly(List<RoleDTO> roleDTOS) {
    if (roleDTOS == null) {
      return null;
    }
    String[] roleArray = new String[roleDTOS.size()];
    int idx = 0;
    for (RoleDTO roleDTO : roleDTOS) {
      roleArray[idx] = roleDTO.getEntityId();
      idx++;
    }
    return roleArray;
  }

  public static List<UserDTO> convert(List<User> users) {
    List<UserDTO> userDTOS = null;
    if (users == null) {
      return null;
    }
    for (User user : users) {
      if (userDTOS == null) {
        userDTOS = new LinkedList<UserDTO>();
      }
//
//      UserDTO userDTO = new UserDTO();
//      userDTO.setEntityId(user.getEntityId());
//      userDTO.setAclRead(user.getAclRead());
//      userDTO.setAclWrite(user.getAclWrite());
//
//      userDTO.setPassword(user.getPassword());
//      userDTO.setUsername(user.getUsername());
//      userDTO.setEmail(user.getEmail());
//      userDTO.setWebToken(user.getWebToken());
//
//      userDTO.setPublicRead(user.getPublicRead());
//      userDTO.setPublicWrite(user.getPublicWrite());
//
//      userDTO.setDateCreated(user.getDateCreated());
//      userDTO.setDateUpdated(user.getDateUpdated());
//
//      if (user.getRoles() != null) {
//        List<RoleDTO> roles = null;
//        for (Role role : user.getRoles()) {
//          if (roles == null) {
//            roles = new LinkedList<>();
//          }
//          roles.add(new RoleDTO(role.getEntityId()));
//        }
//        userDTO.setRoles(roles);
//        userDTOS.add(userDTO);
//      }

      UserDTO userDTO = UserDTO.convert(user);
      userDTOS.add(userDTO);

    }
    return userDTOS;
  }
}
