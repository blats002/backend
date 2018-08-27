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
package com.divroll.roll.resource.jee;

import com.alibaba.fastjson.JSONArray;
import com.divroll.roll.Constants;
import com.divroll.roll.helper.ACLHelper;
import com.divroll.roll.model.Application;
import com.divroll.roll.model.EntityStub;
import com.divroll.roll.model.Role;
import com.divroll.roll.model.Roles;
import com.divroll.roll.repository.RoleRepository;
import com.divroll.roll.repository.UserRepository;
import com.divroll.roll.resource.RolesResource;
import com.divroll.roll.service.WebTokenService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Status;
import scala.actors.threadpool.Arrays;

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
    @Named("defaultRoleStore")
    String storeName;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Roles getRoles() {

        int skipValue = 0;
        int limitValue = DEFAULT_LIMIT;

        if (skip != null && limit != null) {
            skipValue = skip;
            limitValue = limit;
        }

        Application app = applicationService.read(appId);
        if (app == null) {
            return null;
        }

        if (!isMaster(appId, masterKey)) {

            String authUserId = null;

            try {
                authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            } catch (Exception e) {
                // do nothing
            }
            List<Role> results = roleRepository.listRoles(appId, storeName, authUserId,
                    skipValue, limitValue, sort, false);
            Roles roles = new Roles();
            roles.setResults(results);
            roles.setLimit(Long.valueOf(limitValue));
            roles.setSkip(Long.valueOf(skipValue));
            setStatus(Status.SUCCESS_OK);
            return roles;
        } else {
            List<Role> results = roleRepository.listRoles(appId, storeName, null,
                    skipValue, limitValue, sort, true);
            Roles roles = new Roles();
            roles.setResults(results);
            roles.setLimit(Long.valueOf(limitValue));
            roles.setSkip(Long.valueOf(skipValue));
            setStatus(Status.SUCCESS_OK);
            return roles;
        }

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

            List<EntityStub> aclReadList = entity.getAclRead();
            List<EntityStub> aclWriteList = entity.getAclWrite();

            if (aclReadList == null) {
                aclReadList = new LinkedList<>();
            }

            if (aclWriteList == null) {
                aclWriteList = new LinkedList<>();
            }

            if (ACLHelper.contains("", aclReadList) || ACLHelper.contains("", aclWriteList)) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid ACL");
                return null;
            }

            String[] read = new String[]{};
            String[] write = new String[]{};

            if (aclRead != null && !(aclRead.length() == 0)) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclRead);
                    if(!ACLHelper.validate(jsonArray)) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_INVALID_ACL);
                        return null;
                    }
                    read = ACLHelper.onlyIds(jsonArray);
                } catch (Exception e) {
                    // do nothing
                }
            }

            if (aclWrite != null && !(aclWrite.length() == 0)) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclWrite);
                    if(!ACLHelper.validate(jsonArray)) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_INVALID_ACL);
                        return null;
                    }
                    write = ACLHelper.onlyIds(jsonArray);
                } catch (Exception e) {
                    // do nothing
                }
            }

            String roleName = entity.getName();
            publicRead = entity.getPublicRead() != null ? entity.getPublicRead() : true;
            publicWrite = entity.getPublicWrite() != null ? entity.getPublicWrite() : true;

            String roleId = null;
            roleId = roleRepository.createRole(appId, storeName, roleName, read, write, publicRead, publicWrite);

            if (roleId != null) {
                setStatus(Status.SUCCESS_CREATED);
                Role role = new Role();
                role.setName(roleName);
                role.setEntityId(roleId);
                role.setAclWrite(ACLHelper.convert(write));
                role.setAclRead(ACLHelper.convert(read));
                role.setPublicRead(publicRead);
                role.setPublicWrite(publicWrite);
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