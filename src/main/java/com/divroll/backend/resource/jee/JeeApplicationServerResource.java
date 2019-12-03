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

import com.divroll.backend.helper.ComparableMapBuilder;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.Email;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.model.UserRoot;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.repository.SuperuserRepository;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.ApplicationResource;
import com.divroll.backend.xodus.XodusStore;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityId;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.UUID;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeApplicationServerResource extends BaseServerResource
    implements ApplicationResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeApplicationServerResource.class);

  @Inject
  @Named("xodusRoot")
  String xodusRoot;

  @Inject
  @Named("defaultRoleStore")
  String defaultRoleStore;

  @Inject
  @Named("defaultUserStore")
  String defaultUserStore;

  @Inject
  @Named("masterToken")
  String theMasterToken;

  @Inject XodusStore store;

  @Inject UserRepository userRepository;

  @Inject RoleRepository roleRepository;

  @Inject SuperuserRepository superuserRepository;

  @Override
  public Application updateApp(Application entity) {
    try {
      if ((masterKey == null || masterKey.isEmpty()) || (appId == null || appId.isEmpty())) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }
      if (entity == null) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
      }
      String newApiKey = entity.getApiKey();
      String newMasterKey = entity.getMasterKey();
      Email email = entity.getEmailConfig();
      String cloudCode = entity.getCloudCode();
      Application app = applicationService.read(appId);
      if (app != null) {
        String encryptedMasterKey = app.getMasterKey();
        if (BCrypt.checkpw(masterKey, encryptedMasterKey)) {

          if (newApiKey != null && !newApiKey.isEmpty()) {
            String hashApiKey = BCrypt.hashpw(newApiKey, BCrypt.gensalt());
            app.setApiKey(hashApiKey);
          }

          if (newMasterKey != null && !newMasterKey.isEmpty()) {
            String hashMasterkEy = BCrypt.hashpw(newMasterKey, BCrypt.gensalt());
            app.setMasterKey(hashMasterkEy);
          }

          if (email != null) {
            app.setEmailConfig(email);
          }

          if (cloudCode != null) {
            app.setCloudCode(cloudCode);
          }

          applicationService.update(app, encryptedMasterKey);

          setStatus(Status.SUCCESS_OK);
          return app;
        } else {
          setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
      } else {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public Representation retrieveApp() {
    // TODO: Check if app exists
    if(getApp() != null) {
      setStatus(Status.SUCCESS_NO_CONTENT);
    } else {
      setStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }
    return null;
  }

  @Override
  public Application createApp(Application application) {

    if (appName == null) {
      appName = getQueryValue("appName");
    }

    if (appName == null) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
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

    Boolean isMaster = false;
    if (theMasterToken != null
            && masterToken != null
            && BCrypt.checkpw(masterToken, theMasterToken)) {
      isMaster = true;
    }

    if(!isMaster && superAuthToken == null) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return null;
    }

    Superuser superuser = superuserRepository.getUserByAuthToken(superAuthToken);
    if(!isMaster && superuser == null) {
      setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      return null;
    }


    final EntityId id;
    if(isMaster) {
      id = applicationService.create(application, null);
    } else {
      id = applicationService.create(application, superuser);
    }

    if (id != null) {
      if (rootDTO != null) {
        if (beforeSave(
            application,
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
                application,
                ComparableMapBuilder.newBuilder()
                    .put("entityId", roleId)
                    .put("name", rootDTO.getRole())
                    .build(),
                appId,
                defaultUserStore);
          }
          if (beforeSave(
              application,
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
                  application,
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
          return null;
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
