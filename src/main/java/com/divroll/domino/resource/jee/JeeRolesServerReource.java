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
package com.divroll.domino.resource.jee;

import com.alibaba.fastjson.JSONArray;
import com.divroll.domino.Constants;
import com.divroll.domino.model.Role;
import com.divroll.domino.model.Roles;
import com.divroll.domino.repository.RoleRepository;
import com.divroll.domino.repository.UserRepository;
import com.divroll.domino.resource.RolesResource;
import com.divroll.domino.service.WebTokenService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Status;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeRolesServerReource extends BaseServerResource
    implements RolesResource {

    @Inject
    @Named("defaultUserStore")
    String storeName;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Roles getRoles() {
        String skip = getQueryValue(Constants.QUERY_SKIP);
        String limit = getQueryValue(Constants.QUERY_LIMIT);
        if(!isMaster(appId, masterKey)) {

        } else {
            List<Role> results = roleRepository.listRoles(appId, storeName, Long.valueOf(skip), Long.valueOf(limit));
            Roles roles = new Roles();
            roles.setResults(results);
            roles.setLimit(Long.valueOf(limit));
            roles.setSkip(Long.valueOf(skip));
            setStatus(Status.SUCCESS_OK);
            return roles;
        }
        return null;
    }

    @Override
    public Role createRole(Role entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entity == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            String[] read = new String[]{Constants.ACL_ASTERISK};
            String[] write = new String[]{Constants.ACL_ASTERISK};

            if (aclRead != null) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclRead);
                    List<String> aclReadList = new LinkedList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        aclReadList.add(jsonArray.getString(i));
                    }
                    read = aclReadList.toArray(new String[aclReadList.size()]);
                } catch (Exception e) {
                    // do nothing
                }
            }

            if (aclWrite != null) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclWrite);
                    List<String> aclWriteList = new LinkedList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        aclWriteList.add(jsonArray.getString(i));
                    }
                    write = aclWriteList.toArray(new String[aclWriteList.size()]);
                } catch (Exception e) {
                    // do nothing
                }
            }

            String roleName = entity.getName();
            String roleId = null;
            roleId = roleRepository.createRole(appId, storeName, roleName, read, write);

            if(roleId != null) {
                setStatus(Status.SUCCESS_CREATED);
                Role role = new Role();
                role.setName(roleName);
                role.setEntityId(roleId);
                return role;
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }


}
