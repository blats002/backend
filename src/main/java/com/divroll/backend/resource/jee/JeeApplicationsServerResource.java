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

import com.divroll.backend.helper.ComparableMapBuilder;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.Applications;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.model.UserRoot;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.repository.SuperuserRepository;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.ApplicationsResource;
import com.divroll.backend.service.ApplicationService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityId;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeApplicationsServerResource extends BaseServerResource
    implements ApplicationsResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeApplicationsServerResource.class);

  @Inject
  @Named("xodusRoot")
  String xodusRoot;

  @Inject
  UserRepository userRepository;

  @Inject
  RoleRepository roleRepository;

  @Inject
  SuperuserRepository superuserRepository;

  @Inject
  @Named("defaultRoleStore")
  String defaultRoleStore;

  @Inject
  @Named("defaultUserStore")
  String defaultUserStore;

  @Inject
  @Named("masterToken")
  String theMasterToken;

  @Inject ApplicationService applicationService;

  @Override
  public Representation list() {
    try {
      if (theMasterToken != null
          && masterToken != null
          && BCrypt.checkpw(masterToken, theMasterToken)) {
        List<Application> results = applicationService.list(filters, skip, limit, null);
        Applications applications = new Applications();
        applications.setSkip(skip);
        applications.setLimit(limit);
        applications.setResults(results);
        setStatus(Status.SUCCESS_OK);
        return new JsonRepresentation(applications.asJSONObject());
      } else if(superAuthToken != null) {
        Superuser superuser = superuserRepository.getUserByAuthToken(superAuthToken);
        if(superuser == null) {
          setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
          return null;
        }
        List<Application> results = applicationService.list(filters, skip, limit, superuser);
        Applications applications = new Applications();
        applications.setSkip(skip);
        applications.setLimit(limit);
        applications.setResults(results);
        setStatus(Status.SUCCESS_OK);
        return new JsonRepresentation(applications.asJSONObject());
      } else if (isMaster()) {
        Applications applications = new Applications();
        Application application = applicationService.read(appId);
        applications.getResults().add(application);
        applications.setSkip(skip);
        applications.setLimit(1L);
        setStatus(Status.SUCCESS_OK);
        return new JsonRepresentation(applications.asJSONObject());
      } else {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      }
    } catch (IllegalArgumentException e) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    } catch (Exception e) {
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return null;
  }

  @Override
  public Application createApp(Application application) {

    if(application == null) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return null;
    }

    appName = application.getAppName();

    if (appName == null) {
      appName = getQueryValue("appName");
    }

    if (appName == null) {
      if (application == null) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
      }
      appName = application.getAppName();
      if (appName == null) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
      }
    }

    Boolean isMaster = false;
    if (theMasterToken != null
            && masterToken != null
            && BCrypt.checkpw(masterToken, theMasterToken)) {
      isMaster = true;
    }

    if(!isMaster && superAuthToken == null) {
      setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      return null;
    }

    Superuser superuser = superuserRepository.getUserByAuthToken(superAuthToken);
    if(!isMaster && superuser == null) {
      setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      return null;
    }

    UserRoot rootDTO = application.getUser();

    String appId = UUID.randomUUID().toString().replace("-", "");
    String apiKey = UUID.randomUUID().toString().replace("-", "");
    String masterKey = UUID.randomUUID().toString().replace("-", "");

    application.setAppId(appId);
    application.setApiKey(BCrypt.hashpw(apiKey, BCrypt.gensalt()));
    application.setMasterKey(BCrypt.hashpw(masterKey, BCrypt.gensalt()));
    if (appName != null && !appName.isEmpty()) {
      application.setAppName(appName);
    }

    final EntityId id;

    if(isMaster) {
      id = applicationService.create(application, null);
    } else {
      id = applicationService.create(application, superuser);
    }

    if (id != null) {
      // Application app =  applicationService.read(id.toString());

      if (rootDTO != null) {
        if (beforeSave(
            ComparableMapBuilder.newBuilder().put("name", rootDTO.getRole()).build(),
            appId,
            defaultRoleStore)) {
          String roleId =
              roleRepository.createRole(
                  appId,
                  namespace,
                  defaultRoleStore,
                  rootDTO.getRole(),
                  null,
                  null,
                  false,
                  false,
                  actions);
          if (roleId != null) {
            afterSave(
                ComparableMapBuilder.newBuilder()
                    .put("entityId", roleId)
                    .put("name", rootDTO.getRole())
                    .build(),
                appId,
                defaultUserStore);
          }
          if (beforeSave(
              ComparableMapBuilder.newBuilder()
                  .put("username", rootDTO.getUsername())
                  .put("password", rootDTO.getPassword())
                  .build(),
              appId,
              defaultUserStore)) {
            String userId =
                userRepository.createUser(
                    appId,
                    namespace,
                    defaultUserStore,
                    rootDTO.getUsername(),
                    rootDTO.getPassword(),
                    null,
                    null,
                    null,
                    false,
                    false,
                    new String[] {roleId},
                    actions,
                    null,
                    null,
                    null);
            if (userId != null) {
              afterSave(
                  ComparableMapBuilder.newBuilder()
                      .put("entityId", userId)
                      .put("username", rootDTO.getUsername())
                      .put("password", rootDTO.getPassword())
                      .build(),
                  appId,
                  defaultUserStore);
            }
          } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          }
        } else {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
      }

      if (application != null) {
        application.setAppId(appId);
        application.setApiKey(apiKey);
        application.setMasterKey(masterKey);
        application.setAppName(appName);
        setStatus(Status.SUCCESS_CREATED);
        return application;
      }
    } else {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST, appName + " already exists");
    }
    return null;
  }
}
