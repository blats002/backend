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

import com.divroll.backend.Constants;
import com.divroll.backend.helper.JSON;
import com.divroll.backend.model.action.EntityAction;
import com.divroll.backend.model.action.ImmutableBacklinkAction;
import com.divroll.backend.model.action.ImmutableLinkAction;
import com.divroll.backend.model.builder.CreateOptionBuilder;
import com.divroll.backend.model.builder.EntityMetadataBuilder;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.EntitiesResource;
import com.divroll.backend.service.EntityService;
import com.divroll.backend.service.PubSubService;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException;
import org.json.JSONObject;
import org.restlet.representation.Representation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeEntitiesServerResource extends BaseServerResource implements EntitiesResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeEntitiesServerResource.class);
  private static final Integer DEFAULT_LIMIT = 100;

  @Inject UserRepository userRepository;

  @Inject RoleRepository roleRepository;

  @Inject EntityRepository entityRepository;

  @Inject WebTokenService webTokenService;

  @Inject PubSubService pubSubService;

  @Inject
  @Named("defaultFunctionStore")
  String defaultFunctionStore;

  @Inject
  @Named("defaultUserStore")
  String defaultUserStore;

  @Inject
  @Named("defaultRoleStore")
  String defaultRoleStore;

  @Inject EntityService entityService;

  @Override
  public Representation createEntity(Representation entity) {
    try {
      if (!isAuthorized()) {
        return unauthorized();
      }
      if (entity == null || entity.isEmpty()) {
        return badRequest();
      }
      String dir = appId;
      if (dir != null) {
        JSONObject jsonObject = new JSONObject(entity.getText());
        JSONObject entityJSONObject = jsonObject.getJSONObject("entity");

        if (entityJSONObject == null) {
          return badRequest();
        }

        String authUserId = null;
        try {
          authUserId = webTokenService.readUserIdFromToken(getApp().getMasterKey(), authToken);
        } catch (Exception e) {
          // do nothing
        }

        List<EntityAction> entityActions = new LinkedList<>();
        if (linkName != null && linkFrom != null) {
          boolean isAuth = true;
          if (authUserId == null
              || (!entityRepository.getACLWriteList(appId, namespace, linkFrom).contains(authUserId)
                  && !isMaster())) {
            isAuth = false;
          }
          if (linkFrom.equals(authUserId)) {
            isAuth = true;
          }
          if (entityRepository.isPublicWrite(appId, namespace, linkFrom)) {
            isAuth = true;
          }
          if (!isAuth) {
            return unauthorized();
          }
          entityActions.add(
              ImmutableBacklinkAction.builder().linkName(linkName).entityId(linkFrom).build());
        } else if (authUserId != null && linkName != null && linkTo != null) {
          if (authUserId == null
              || !entityRepository.getACLWriteList(appId, namespace, linkTo).contains(authUserId)
                  && !isMaster()) {
            return unauthorized();
          }
          entityActions.add(
              ImmutableLinkAction.builder().linkName(linkName).entityId(linkFrom).build());
        }

        Map<String, Comparable> comparableMap = JSON.jsonToMap(entityJSONObject);
        JSONObject response =
            entityService.createEntity(
                getApp(),
                namespace,
                entityType,
                comparableMap,
                aclRead,
                aclWrite,
                publicRead,
                publicWrite,
                actions,
                entityActions,
                new CreateOptionBuilder()
                        .createOption(null)
                        .referencePropertyName(null)
                        .build(),
                new EntityMetadataBuilder().uniqueProperties(uniqueProperties).build());
        if (entityType.equals(defaultUserStore)) {
          response.remove(Constants.RESERVED_FIELD_PASSWORD);
        }
        return created(response);
      }
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      return badRequest(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      return serverError();
    }
    return null;
  }

  @Override
  public Representation getEntities() {
    try {
      if (!isAuthorized()) {
        return unauthorized();
      }

      int skipValue = 0;
      int limitValue = DEFAULT_LIMIT;

      if (skip != null && limit != null) {
        skipValue = skip;
        limitValue = limit;
      }

      Long lCount = null;
      if(count != null && Boolean.valueOf(count) == true) {
        lCount = entityRepository.countEntities(appId, namespace, entityType, false, filters);
      }

      if (isMaster()) {
        try {
          List<Map<String, Comparable>> entityObjs =
              entityRepository.listEntities(
                  appId, namespace, entityType, null, skipValue, limitValue, sort, linkName, sourceEntityId, true, filters);
          JSONObject responseBody = new JSONObject();
          JSONObject entitiesJSONObject = new JSONObject();
          entitiesJSONObject.put("results", entityObjs);
          entitiesJSONObject.put("skip", skipValue);
          entitiesJSONObject.put("limit", limitValue);
          if(lCount != null) {
            entitiesJSONObject.put("count", lCount);
          }
          responseBody.put("entities", entitiesJSONObject);
          return success(responseBody);
        } catch (Exception e) {
          return serverError();
        }
      } else {

        String authUserId = null;

        try {
          authUserId = webTokenService.readUserIdFromToken(getApp().getMasterKey(), authToken);
        } catch (Exception e) {
          // do nothing
        }

        try {
          List<Map<String, Comparable>> entityObjs =
              entityRepository.listEntities(
                  appId,
                  namespace,
                  entityType,
                  authUserId,
                  skipValue,
                  limitValue,
                  sort,
                  linkName,
                      sourceEntityId,
                  false,
                  filters);

          JSONObject responseBody = new JSONObject();
          JSONObject entitiesJSONObject = new JSONObject();
          entitiesJSONObject.put("results", entityObjs);
          entitiesJSONObject.put("skip", skipValue);
          entitiesJSONObject.put("limit", limitValue);
          if(lCount != null) {
            entitiesJSONObject.put("count", lCount);
          }
          responseBody.put("entities", entitiesJSONObject);

          return success(responseBody);
        } catch (Exception e) {
          e.printStackTrace();
          return serverError();
        }
      }

    } catch (EntityRemovedInDatabaseException e) {
      e.printStackTrace();
      return notFound();
    } catch (Exception e) {
      e.printStackTrace();
      return serverError();
    }
  }

  @Override
  public Representation deleteEntities() {
    try {
      if (isMaster()) {
        boolean status = entityRepository.deleteEntities(appId, namespace, entityType);
        if (status) {
          pubSubService.deletedAll(appId, namespace, entityType);
          return success();
        } else {
          return badRequest();
        }
      } else {
        return unauthorized();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return serverError();
    }
  }

  @Override
  public Representation updateEntities(Representation representation) {
    if (!isMaster()) {
      return unauthorized();
    }
    try {
      boolean updated =
          entityRepository.updateProperty(
              appId,
              namespace,
              entityType,
              propertyName,
              new EntityMetadataBuilder().uniqueProperties(uniqueProperties).build());
      if (updated) {
        return success();
      } else {
        return badRequest();
      }
    } catch (IllegalArgumentException e) {
      return badRequest(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      return serverError();
    }
  }
}
