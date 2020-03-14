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
import com.divroll.backend.helper.ACLHelper;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.EntityStub;
import com.divroll.backend.model.Role;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.resource.LinkResource;
import com.divroll.backend.service.PubSubService;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeLinkServerResource extends BaseServerResource implements LinkResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeLinkServerResource.class);

  @Inject EntityRepository entityRepository;

  @Inject RoleRepository roleRepository;

  @Inject WebTokenService webTokenService;

  @Inject PubSubService pubSubService;

  @Override
  public void createLink(Representation entity) {
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return;
      }

      Application app = applicationService.read(appId);
      if (app == null) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return;
      }

      String authUserId = null;

      boolean isWriteAccess = false;
      boolean isMaster = false;
      boolean isPublic = false;

      try {
        authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      } catch (Exception e) {
        // do nothing
      }

      Map<String, Comparable> map =
          entityRepository.getEntity(appId, namespace, entityType, entityId, new LinkedList<>());
      List<EntityStub> aclWriteList =
          map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
              ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE)
              : new LinkedList<>();

      if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
        isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
      }

      if (isMaster()) {
        isMaster = true;
      } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
        isWriteAccess = true;
      } else if (authUserId != null) {
        List<Role> roles = roleRepository.getRolesOfEntity(appId, namespace, authUserId);
        for (Role role : roles) {
          if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
            isWriteAccess = true;
          }
        }
      }

      if (isMaster || isWriteAccess || isPublic) {
        Boolean bSet = false;
        if(linkType != null) {
          if(linkType.equals("set")) {
            bSet = true;
          } else if(linkType.equals("add")) {
            bSet = false;
          } else {
            throw new IllegalArgumentException("Invalid linkType " + linkType);
          }
        }
        if (entityRepository.linkEntity(
            appId, namespace, entityType, linkName, entityId, targetEntityId, bSet)) {
          pubSubService.linked(appId, namespace, entityType, linkName, entityId, targetEntityId);
          setStatus(Status.SUCCESS_CREATED);
        } else {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
      } else {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      }

    } catch (Exception e) {
      setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
    }
  }

  @Override
  public void deleteLink(Representation entity) {
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return;
      }

      Application app = applicationService.read(appId);
      if (app == null) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return;
      }

      String authUserId = null;

      boolean isWriteAccess = false;
      boolean isMaster = false;
      boolean isPublic = false;

      try {
        authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      } catch (Exception e) {
        // do nothing
      }

      Map<String, Comparable> map =
          entityRepository.getEntity(appId, namespace, entityType, entityId, new LinkedList<>());

      if (map == null) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      } else {
        List<EntityStub> aclWriteList =
            map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
                ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE)
                : new LinkedList<>();

        if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
          isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
        }

        if (isMaster()) {
          isMaster = true;
        } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
          isWriteAccess = true;
        } else if (authUserId != null) {
          List<Role> roles = roleRepository.getRolesOfEntity(appId, namespace, authUserId);
          for (Role role : roles) {
            if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
              isWriteAccess = true;
            }
          }
        }

        if (isMaster || isWriteAccess || isPublic) {
          if (entityRepository.unlinkEntity(
              appId, namespace, entityType, linkName, entityId, targetEntityId)) {
            pubSubService.unlinked(
                appId, namespace, entityType, linkName, entityId, targetEntityId);
            setStatus(Status.SUCCESS_OK);
          } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          }
        } else {
          setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
      }

    } catch (EntityRemovedInDatabaseException e) {
      setStatus(Status.SERVER_ERROR_INTERNAL, "Entity does not exist or was removed");
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
  }

  @Override
  public Representation checkLink(Representation entity) {
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }
      if (entity == null || entity.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
      }

      Application app = applicationService.read(appId);
      if (app == null) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return null;
      }

      String authUserId = null;

      boolean isWriteAccess = false;
      boolean isMaster = false;
      boolean isPublic = false;

      try {
        authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      } catch (Exception e) {
        // do nothing
      }

      Map<String, Comparable> map =
          entityRepository.getEntity(appId, namespace, entityType, entityId, new LinkedList<>());

      if (map == null) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      } else {
        List<EntityStub> aclWriteList =
            map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
                ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE)
                : new LinkedList<>();

        if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
          isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
        }

        if (isMaster()) {
          isMaster = true;
        } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
          isWriteAccess = true;
        } else if (authUserId != null) {
          List<Role> roles = roleRepository.getRolesOfEntity(appId, namespace, authUserId);
          for (Role role : roles) {
            if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
              isWriteAccess = true;
            }
          }
        }

        if (isMaster || isWriteAccess || isPublic) {
          if (entityRepository.isLinked(
              appId, namespace, entityType, linkName, entityId, targetEntityId)) {
            setStatus(Status.SUCCESS_OK);
          } else {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
          }
        } else {
          setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
      }

    } catch (Exception e) {
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return null;
  }
}
