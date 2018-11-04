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

import com.divroll.backend.helper.ComparableMapBuilder;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.Applications;
import com.divroll.backend.model.Email;
import com.divroll.backend.model.UserRootDTO;
import com.divroll.backend.repository.RoleRepository;
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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeApplicationsServerResource extends BaseServerResource
        implements ApplicationsResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeApplicationsServerResource.class);

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    @Named("defaultRoleStore")
    String roleStoreName;

    @Inject
    @Named("defaultUserStore")
    String userStoreName;

    @Inject
    @Named("masterToken")
    String theMasterToken;

    @Inject
    ApplicationService applicationService;

    @Override
    public Applications list() {
        try {
            // TODO: Add auth
            if(theMasterToken != null
                    && masterToken != null
                    && BCrypt.checkpw(masterToken, theMasterToken)) {
                List<Application> results = applicationService.list(filters, skip, limit);
                Applications applications = new Applications();
                applications.setSkip(skip);
                applications.setLimit(limit);
                applications.setResults(results);
                setStatus(Status.SUCCESS_OK);
                return applications;
            } else if(isMaster()) {
                Applications applications = new Applications();
                Application application = applicationService.read(appId);
                applications.getResults().add(application);
                applications.setSkip(skip);
                applications.setLimit(1L);
                setStatus(Status.SUCCESS_OK);
                return applications;
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public Application createApp(Application application) {

        if(appName == null) {
            appName = getQueryValue("appName");
        }

        if(appName == null) {
            appName = application.getAppName();
            if(appName == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }
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
                if(beforeSave(ComparableMapBuilder.newBuilder().put("name", rootDTO.getRole()).build(), appId, roleStoreName)) {
                    String roleId = roleRepository.createRole(appId, roleStoreName, rootDTO.getRole(), null, null, false, false, actions);
                    if(roleId != null) {
                        afterSave(ComparableMapBuilder.newBuilder().put("entityId", roleId).put("name", rootDTO.getRole()).build(), appId, userStoreName);
                    }
                    if(beforeSave(ComparableMapBuilder.newBuilder().put("username", rootDTO.getUsername()).put("password", rootDTO.getPassword()).build(), appId, userStoreName)) {
                        String userId = userRepository.createUser(appId, userStoreName, rootDTO.getUsername(), rootDTO.getPassword(),
                                null, null, null, false, false,
                                new String[]{roleId}, actions, null, null, null);
                        if(userId != null) {
                            afterSave(ComparableMapBuilder.newBuilder().put("entityId", userId).put("username", rootDTO.getUsername()).put("password", rootDTO.getPassword()).build(), appId, userStoreName);
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
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

}
