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
package com.divroll.backend.resource.jee;

import com.alibaba.fastjson.JSONArray;
import com.divroll.backend.Constants;
import com.divroll.backend.helper.ACLHelper;
import com.divroll.backend.helper.ComparableMapBuilder;
import com.divroll.backend.helper.ObjectLogger;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.EntityStub;
import com.divroll.backend.model.Role;
import com.divroll.backend.model.Roles;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.RolesResource;
import com.divroll.backend.service.PubSubService;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
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
public class JeeRolesServerReource extends BaseServerResource implements RolesResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeRolesServerReource.class);

  @Inject
  @Named("defaultRoleStore")
  String defaultRoleStore;

  @Inject UserRepository userRepository;

  @Inject RoleRepository roleRepository;

  @Inject WebTokenService webTokenService;

  @Inject PubSubService pubSubService;

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

    if (!isMaster()) {

      String authUserId = null;

      try {
        authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      } catch (Exception e) {
        // do nothing
      }
      List<Role> results =
          roleRepository.listRoles(
              appId,
              namespace,
              defaultRoleStore,
              authUserId,
              skipValue,
              limitValue,
              sort,
              false,
              filters);
      Roles roles = new Roles();
      roles.setResults(results);
      roles.setLimit(Long.valueOf(limitValue));
      roles.setSkip(Long.valueOf(skipValue));
      setStatus(Status.SUCCESS_OK);
      return roles;
    } else {
      List<Role> results =
          roleRepository.listRoles(
              appId, namespace, defaultRoleStore, null, skipValue, limitValue, sort, true, filters);
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
      if (!isAuthorized()) {
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

      String[] read = null;
      String[] write = null;

      if ((aclReadList == null || aclReadList.isEmpty()) && aclRead != null) {
        try {
          JSONArray jsonArray = JSONArray.parseArray(aclRead);
          if (jsonArray != null) {
            if (!ACLHelper.validate(jsonArray)) {
              setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_INVALID_ACL);
              return null;
            }
            read = ACLHelper.onlyIds(jsonArray);
          }
        } catch (Exception e) {
          // do nothing
        }
      } else {
        read = ACLHelper.onlyIds(aclReadList);
      }

      if ((aclWriteList == null || aclWriteList.isEmpty()) && aclWrite != null) {
        try {
          JSONArray jsonArray = JSONArray.parseArray(aclWrite);
          if (jsonArray != null) {
            if (!ACLHelper.validate(jsonArray)) {
              setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_INVALID_ACL);
              return null;
            }
            write = ACLHelper.onlyIds(jsonArray);
          }
        } catch (Exception e) {
          // do nothing
        }
      } else {
        write = ACLHelper.onlyIds(aclWriteList);
      }

      String roleName = entity.getName();
      publicRead = entity.getPublicRead() != null ? entity.getPublicRead() : true;
      publicWrite = entity.getPublicWrite() != null ? entity.getPublicWrite() : true;

      String roleId = null;
      validateIds(read, write);
      if (beforeSave(
          ComparableMapBuilder.newBuilder().put("name", roleName).build(), appId, entityType)) {
        roleId =
            roleRepository.createRole(
                appId,
                namespace,
                defaultRoleStore,
                roleName,
                read,
                write,
                publicRead,
                publicWrite,
                actions);
        if (roleId != null) {
          afterSave(
              ComparableMapBuilder.newBuilder()
                  .put("entityId", roleId)
                  .put("name", roleName)
                  .build(),
              appId,
              entityType);
        }
      } else {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
      }
      if (roleId != null) {
        pubSubService.created(appId, namespace, defaultRoleStore, entityId);
        setStatus(Status.SUCCESS_CREATED);
        Role role = new Role();
        role.setName(roleName);
        role.setEntityId(roleId);
        role.setAclWrite(ACLHelper.convert(write));
        role.setAclRead(ACLHelper.convert(read));
        role.setPublicRead(publicRead);
        role.setPublicWrite(publicWrite);
        return (Role) ObjectLogger.log(role);
      } else {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      }

    } catch (IllegalArgumentException e) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return null;
  }
}
