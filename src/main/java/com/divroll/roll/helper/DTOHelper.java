package com.divroll.roll.helper;

import com.divroll.roll.model.Role;
import com.divroll.roll.model.RoleDTO;
import com.divroll.roll.model.User;
import com.divroll.roll.model.UserDTO;

import java.util.LinkedList;
import java.util.List;

public class DTOHelper {
    public static String[] roleIdsOnly(List<RoleDTO> roleDTOS) {
        if(roleDTOS == null) {
            return null;
        }
        String[] roleArray = new String[roleDTOS.size()];
        int idx = 0;
        for(RoleDTO roleDTO : roleDTOS) {
            roleArray[idx] = roleDTO.getEntityId();
            idx++;
        }
        return roleArray;
    }
    public static List<UserDTO> convert(List<User> users) {
        List<UserDTO> userDTOS = null;
        if(users == null) {
            return null;
        }
        for(User user : users) {
            if(userDTOS == null) {
                userDTOS = new LinkedList<UserDTO>();
            }
            UserDTO userDTO = new UserDTO();
            userDTO.setEntityId(user.getEntityId());
            userDTO.setAclRead(user.getAclRead());
            userDTO.setAclWrite(user.getAclWrite());

            userDTO.setPassword(user.getPassword());
            userDTO.setUsername(user.getUsername());
            userDTO.setWebToken(user.getWebToken());

            userDTO.setPublicRead(user.getPublicRead());
            userDTO.setPublicWrite(user.getPublicWrite());

            if(user.getRoles() != null) {
                List<RoleDTO> roles = null;
                for(Role role : user.getRoles()) {
                    if(roles == null) {
                        roles = new LinkedList<>();
                    }
                    roles.add(new RoleDTO(role.getEntityId()));
                }
                userDTO.setRoles(roles);
                userDTOS.add(userDTO);
            }
        }
        return userDTOS;
    }
}
