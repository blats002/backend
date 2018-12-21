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
package com.divroll.backend.resource.jee;

import com.alibaba.fastjson.JSONArray;
import com.divroll.backend.Constants;
import com.divroll.backend.helper.ACLHelper;
import com.divroll.backend.helper.ComparableMapBuilder;
import com.divroll.backend.helper.DTOHelper;
import com.divroll.backend.helper.ObjectLogger;
import com.divroll.backend.model.*;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.UserResource;
import com.divroll.backend.service.PubSubService;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.Representation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeUserServerResource extends BaseServerResource implements UserResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeUserServerResource.class);

  @Inject
  @Named("defaultUserStore")
  String defaultUserStore;

  @Inject UserRepository userRepository;

  @Inject WebTokenService webTokenService;

  @Inject PubSubService pubSubService;

  @Override
  public Representation getUser() { // login
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }
      String username = getQueryValue(Constants.QUERY_USERNAME);
      String password = getQueryValue(Constants.QUERY_PASSWORD);
      Application app = applicationService.read(appId);

      LOG.with("NAMESPACE", namespace);
      System.out.println("NAMESPACE="+namespace);

      if (app == null) {
        return null;
      }

      if (validateId(userId)) {
        if (isMaster()) {
          User userEntity = userRepository.getUser(appId, namespace, defaultUserStore, userId, includeLinks);
          if (userEntity == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
          }
          userEntity.setPassword(null);
          setStatus(Status.SUCCESS_OK);

          Representation representation = new XstreamRepresentation<UserDTO>(MediaType.APPLICATION_JSON, UserDTO.convert((User) ObjectLogger.log(userEntity)));
          String text = representation.getText();
          System.out.println("--------------------->" + text);
          return representation;
        } else {
          String authUserId = null;
          if (authToken != null) {
            authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
          }
          Boolean isAccess = false;

          User userEntity = userRepository.getUser(appId, namespace, defaultUserStore, userId, includeLinks);

          if (userEntity == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
          }

          Boolean publicRead =
              userEntity.getPublicRead() != null ? userEntity.getPublicRead() : false;
          if (authUserId != null && userEntity.getAclRead().contains(authUserId)) {
            isAccess = true;
          }
          if (!publicRead && !isAccess) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
          }
          if (userEntity != null) {
            setStatus(Status.SUCCESS_OK);
            Representation representation = new XstreamRepresentation<UserDTO>(MediaType.APPLICATION_JSON, UserDTO.convert((User) ObjectLogger.log(userEntity)));
            String text = representation.getText();
            System.out.println("--------------------->" + text);
            return representation;
          } else {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
          }
        }
      } else { // login

        if (username == null && authToken == null) {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_MISSING_USERNAME_PASSWORD);
          return null;
        }

        User userEntity = null;

        if (authToken != null) {
          String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
          if (authUserId == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
          }
          userEntity = userRepository.getUser(appId, namespace, defaultUserStore, authUserId, includeLinks);
          if (userEntity == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
          }
          User user = new User();
          user.setEntityId(userEntity.getEntityId());
          user.setUsername(userEntity.getUsername());
          user.setPassword(null);
          user.setAclRead(userEntity.getAclRead());
          user.setAclWrite(userEntity.getAclWrite());
          user.setPublicRead(userEntity.getPublicRead());
          user.setPublicWrite(userEntity.getPublicWrite());
          user.setRoles(userEntity.getRoles());
          user.setLinks(userEntity.getLinks());
          setStatus(Status.SUCCESS_OK);

          Representation representation = new XstreamRepresentation<UserDTO>(MediaType.APPLICATION_JSON,
                  UserDTO.convert((User) ObjectLogger.log(user)));
          String text = representation.getText();
          System.out.println("--------------------->" + text);
          return representation;
        } else {
          userEntity =
              userRepository.getUserByUsername(appId, namespace, defaultUserStore, username, includeLinks);
          if (userEntity == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
          }
          String userId = userEntity.getEntityId();
          String existingPassword = userEntity.getPassword();
          if (BCrypt.checkpw(password, existingPassword)) {
            String webToken = webTokenService.createToken(app.getMasterKey(), userId);
            User user = new User();
            user.setEntityId(userId);
            user.setWebToken(webToken);
            user.setPassword(null);
            user.setLinks(userEntity.getLinks());
            setStatus(Status.SUCCESS_OK);

            Representation representation = new XstreamRepresentation<UserDTO>(MediaType.APPLICATION_JSON, UserDTO.convert((User) ObjectLogger.log(user)));
            String text = representation.getText();
            System.out.println("--------------------->" + text);
            return representation;
          } else {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
          }
        }
      }

      setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      return null;
    } catch (EntityRemovedInDatabaseException e) {
      e.printStackTrace();
      setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
      return null;
    }
  }

  @Override
  public Representation updateUser(UserDTO entity) {
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }

      if (!validateId(userId)) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
      }

      if (entity == null) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
      }

      String newUsername = entity.getUsername();
      String newPlainPassword = entity.getPassword();
      publicRead = entity.getPublicRead() != null ? entity.getPublicRead() : true;
      publicWrite = entity.getPublicWrite() != null ? entity.getPublicWrite() : true;

      //            if (newUsername == null || newPlainPassword == null) {
      //                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      //                return null;
      //            }

      Application app = applicationService.read(appId);
      if (app == null) {
        return null;
      }

      String[] read = new String[] {};
      String[] write = new String[] {};

      List<EntityStub> aclReadList = entity.getAclRead();
      List<EntityStub> aclWriteList = entity.getAclWrite();

      if (aclReadList == null) {
        aclReadList = new LinkedList<>();
      }

      if (aclWriteList == null) {
        aclWriteList = new LinkedList<>();
      }

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

      final User user = userRepository.getUser(appId, namespace, defaultUserStore, userId, includeLinks);
      if (user == null) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return null;
      }

      List<RoleDTO> roles = entity.getRoles();
      String[] roleArray = DTOHelper.roleIdsOnly(roles);

      Boolean isMaster = isMaster();

      if (isMaster || (user.getPublicWrite() != null && user.getPublicWrite())) {
        String newHashPassword = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());
        validateIds(read, write);
        Boolean success = false;
        if (beforeSave(
            ComparableMapBuilder.newBuilder()
                .put("entityId", entityId)
                .put("username", newUsername)
                .build(),
            appId,
            entityType)) {
          success =
              userRepository.updateUser(
                  appId,
                  namespace,
                  defaultUserStore,
                  userId,
                  newUsername,
                  newHashPassword,
                  null,
                  read,
                  write,
                  publicRead,
                  publicWrite,
                  roleArray);
          if (success) {
            afterSave(
                ComparableMapBuilder.newBuilder()
                    .put("entityId", entityId)
                    .put("username", newUsername)
                    .build(),
                appId,
                entityType);
          }
        } else {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        if (success) {
          User resultUser = new User();
          resultUser.setEntityId(userId);
          resultUser.setUsername(newUsername);
          resultUser.setPassword(null);
          resultUser.setAclRead(ACLHelper.convert(read));
          resultUser.setAclWrite(ACLHelper.convert(write));
          resultUser.setPublicRead(publicRead);
          resultUser.setPublicWrite(publicWrite);
          for (Object roleId : Arrays.asList(roleArray)) {
            resultUser.getRoles().add(new Role((String) roleId));
          }
          pubSubService.updated(appId, namespace, defaultUserStore, userId);
          setStatus(Status.SUCCESS_OK);

          Representation representation = new XstreamRepresentation<UserDTO>(MediaType.APPLICATION_JSON, UserDTO.convert((User) ObjectLogger.log(resultUser)));
          String text = representation.getText();
          System.out.println("--------------------->" + text);
          return representation;
        } else {
          setStatus(Status.SERVER_ERROR_INTERNAL);
        }
      } else {
        String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
        boolean isAccess = false;
        if (authUserId != null) {
          if (authUserId.equals(user.getEntityId())) {
            isAccess = true;
          } else {
            final User authUser =
                userRepository.getUser(appId, namespace, defaultUserStore, authUserId, includeLinks);
            for (Role role : authUser.getRoles()) {
              String roleId = role.getEntityId();
              if (ACLHelper.contains(roleId, user.getAclWrite())) {
                isAccess = true;
              }
            }
            if (!isAccess
                && user.getAclWrite() != null
                && ACLHelper.contains(authUserId, user.getAclWrite())) {
              isAccess = true;
            }
          }
          if (isAccess) {
            String newHashPassword = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());
            Boolean success = false;
            if (beforeSave(
                ComparableMapBuilder.newBuilder()
                    .put("entityId", entityId)
                    .put("username", newUsername)
                    .build(),
                appId,
                entityType)) {
              success =
                  userRepository.updateUser(
                      appId,
                      namespace,
                      defaultUserStore,
                      userId,
                      newUsername,
                      newHashPassword,
                      null,
                      read,
                      write,
                      publicRead,
                      publicWrite,
                      roleArray);
              if (success) {
                afterSave(
                    ComparableMapBuilder.newBuilder()
                        .put("entityId", entityId)
                        .put("username", newUsername)
                        .build(),
                    appId,
                    entityType);
              }
            } else {
              setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
            if (success) {
              User resultUser = new User();
              resultUser.setEntityId(userId);
              resultUser.setUsername(newUsername);
              resultUser.setPassword(null);
              resultUser.setAclRead(ACLHelper.convert(read));
              resultUser.setAclWrite(ACLHelper.convert(write));
              resultUser.setPublicRead(publicRead);
              resultUser.setPublicWrite(publicWrite);
              for (Object roleId : Arrays.asList(roleArray)) {
                resultUser.getRoles().add(new Role((String) roleId));
              }
              pubSubService.updated(appId, namespace, defaultUserStore, userId);
              setStatus(Status.SUCCESS_OK);

              Representation representation = new XstreamRepresentation<UserDTO>(MediaType.APPLICATION_JSON, UserDTO.convert((User) ObjectLogger.log(resultUser)));
              String text = representation.getText();
              System.out.println("--------------------->" + text);
              return representation;
            } else {
              setStatus(Status.SERVER_ERROR_INTERNAL);
            }
          } else {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
          }
        } else {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_INVALID_AUTH_TOKEN);
        }
      }
    } catch (IllegalArgumentException e) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return null;
  }

  @Override
  public void deleteUser(UserDTO entity) {
    try {

      Application app = applicationService.read(appId);
      if (app == null) {
        return;
      }

      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return;
      }

      if (userId == null && username == null) {
        if (userId == null) {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_MISSING_USER_ID);
          return;
        }
        if (username == null) {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_QUERY_USERNAME_REQUIRED);
          return;
        }
      }

      boolean isMaster = false;
      boolean publicWrite = false;

      User userEntity = null;

      if (userId != null) {
        userEntity = userRepository.getUser(appId, namespace, defaultUserStore, userId, null);
      }
      if (username != null) {
        userEntity = userRepository.getUserByUsername(appId, namespace, defaultUserStore, username, null);
      }

      String id = userEntity.getEntityId();
      publicWrite = userEntity.getPublicWrite();

      String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      boolean isAccess = false;

      if (authUserId != null && ACLHelper.contains(authUserId, userEntity.getAclWrite())) {
        isAccess = true;
      } else if (authUserId != null) {
        final User authUser =
            userRepository.getUser(appId, namespace, defaultUserStore, authUserId, null);
        for (Role role : authUser.getRoles()) {
          String roleId = role.getEntityId();
          if (ACLHelper.contains(roleId, userEntity.getAclWrite())) {
            isAccess = true;
          }
        }
      }

      if (isMaster || isAccess || publicWrite) {
        if (userRepository.deleteUser(appId, namespace, defaultUserStore, id)) {
          pubSubService.deleted(appId, namespace, defaultUserStore, entityId);
          setStatus(Status.SUCCESS_OK);
        } else {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_CANNOT_DELETE_USER);
        }
      } else {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return;
      }

    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return;
  }
}
