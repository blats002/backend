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
package com.divroll.backend.resource.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.Role;
import com.divroll.backend.model.User;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.UserRoleResource;
import com.divroll.backend.service.PubSubService;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeUserRoleServerResource extends BaseServerResource implements UserRoleResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeUserRoleServerResource.class);

  @Inject
  @Named("defaultUserStore")
  String defaultUserStore;

  @Inject UserRepository userRepository;

  @Inject RoleRepository roleRepository;

  @Inject WebTokenService webTokenService;

  @Inject PubSubService pubSubService;

  @Override
  public void createUserRoleLink(Representation entity) {
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return;
      }
      if (entity == null) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return;
      }

      Application app = applicationService.read(appId);
      if (app != null) {
        String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
        Role role = roleRepository.getRole(appId, namespace, defaultUserStore, roleId);
        User user = userRepository.getUser(appId, namespace, defaultUserStore, userId, null);
        User authUser = userRepository.getUser(appId, namespace, defaultUserStore, authUserId, null);
        if (authUser == null || user == null || role == null) {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          return;
        }
        if (isUserAuthForRole(user, role)) {
          boolean success =
              roleRepository.linkRole(
                  appId, namespace, defaultUserStore, role.getEntityId(), user.getEntityId());
          if (success) {
            pubSubService.linked(
                appId,
                namespace,
                defaultUserStore,
                Constants.ROLE_LINKNAME,
                role.getEntityId(),
                user.getEntityId());
            setStatus(Status.SUCCESS_CREATED);
          } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          }
        } else {
          setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
      } else {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_APPLICATION_NOT_FOUND);
      }

    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return;
  }

  @Override
  public void deleteUserRoleLink(Representation entity) {
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return;
      }
      if (entity == null) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return;
      }

      Application app = applicationService.read(appId);
      if (app != null) {
        String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
        Role role = roleRepository.getRole(appId, namespace, defaultUserStore, roleId);
        User user = userRepository.getUser(appId, namespace, defaultUserStore, userId, null);
        User authUser = userRepository.getUser(appId, namespace, defaultUserStore, authUserId, null);
        if (authUser == null || user == null || role == null) {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          return;
        }
        if (isUserAuthForRole(user, role)) {
          boolean success =
              roleRepository.unlinkRole(
                  appId, namespace, defaultUserStore, role.getEntityId(), user.getEntityId());
          if (success) {
            pubSubService.unlinked(
                appId,
                namespace,
                defaultUserStore,
                Constants.ROLE_LINKNAME,
                role.getEntityId(),
                user.getEntityId());
            setStatus(Status.SUCCESS_CREATED);
          } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          }
        } else {
          setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
      } else {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_APPLICATION_NOT_FOUND);
      }

    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return;
  }

  private boolean isUserAuthForRole(User user, Role role) {
    try {
      boolean isLinked =
          roleRepository.isLinked(
              appId, namespace, defaultUserStore, role.getEntityId(), user.getEntityId());
      return isLinked;
    } catch (Exception e) {

    }
    return false;
  }
}
