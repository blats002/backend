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

import com.divroll.backend.model.Application;
import com.divroll.backend.model.Email;
import com.divroll.backend.model.UserRootDTO;
import com.divroll.backend.repository.RoleRepository;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeApplicationServerResource extends BaseServerResource
        implements ApplicationResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeApplicationServerResource.class);

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    @Named("defaultRoleStore")
    String roleStoreName;

    @Inject
    @Named("defaultUserStore")
    String userStoreName;

    @Inject
    XodusStore store;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Override
    public Application updateApp(Application entity) {
        try {
            if ((masterKey == null || masterKey.isEmpty()) || (appId == null || appId.isEmpty())) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if(entity == null) {
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

                    if(newApiKey != null && !newApiKey.isEmpty()) {
                        String hashApiKey = BCrypt.hashpw(newApiKey, BCrypt.gensalt());
                        app.setApiKey(hashApiKey);
                    }

                    if(newMasterKey != null && !newMasterKey.isEmpty()) {
                        String hashMasterkEy = BCrypt.hashpw(newMasterKey, BCrypt.gensalt());
                        app.setMasterKey(hashMasterkEy);
                    }

                    if(email != null) {
                        app.setEmailConfig(email);
                    }

                    if(cloudCode != null) {
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
    public Application createApp(Application application) {

        if(appName == null) {
            appName = getQueryValue("appName");
        }

        if(appName == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }

        UserRootDTO rootDTO = application.getUser();

        String appId = UUID.randomUUID().toString().replace("-", "");
        String apiKey = UUID.randomUUID().toString().replace("-", "");
        String masterKey = UUID.randomUUID().toString().replace("-", "");

        application.setAppId(appId);
        application.setApiKey(BCrypt.hashpw(apiKey, BCrypt.gensalt()));
        application.setMasterKey(BCrypt.hashpw(masterKey, BCrypt.gensalt()));
        if(appName != null && !appName.isEmpty()) {
            application.setAppName(appName);
        }

        final EntityId id = applicationService.create(application);
        if (id != null) {
            //Application app =  applicationService.read(id.toString());

            if(rootDTO != null) {
                String roleId = roleRepository.createRole(appId, roleStoreName, rootDTO.getRole(), null, null, false, false);
                String userId = userRepository.createUser(appId, userStoreName, rootDTO.getUsername(), rootDTO.getPassword(),
                        null, null, null, false, false,
                        new String[]{roleId});
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
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

}
