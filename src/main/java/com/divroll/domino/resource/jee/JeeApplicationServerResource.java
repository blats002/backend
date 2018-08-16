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
package com.divroll.domino.resource.jee;

import com.divroll.domino.model.Application;
import com.divroll.domino.resource.ApplicationResource;
import com.divroll.domino.xodus.XodusStore;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityId;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;

import java.util.UUID;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeApplicationServerResource extends BaseServerResource
        implements ApplicationResource {

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    XodusStore store;


    @Override
    public Application updateApp(Application entity) {
        if ((masterKey == null || masterKey.isEmpty()) || (appId == null || appId.isEmpty())) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }
        String newApiKey = entity.getApiKey();
        String newMasterKey = entity.getMasterKey();
        Application app = applicationService.read(appId);
        if (app != null) {
            String encryptedMasterKey = app.getMasterKey();
            if (BCrypt.checkpw(masterKey, encryptedMasterKey)) {
                String hashApiKey = BCrypt.hashpw(newApiKey, BCrypt.gensalt());
                String hashMasterkEy = BCrypt.hashpw(newMasterKey, BCrypt.gensalt());
                app.setApiKey(hashApiKey);
                app.setMasterKey(hashMasterkEy);
                applicationService.update(app, encryptedMasterKey);
                setStatus(Status.SUCCESS_OK);
                app.setApiKey(newApiKey);
                app.setMasterKey(newMasterKey);
                return app;
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
        } else {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
        return null;
    }

    @Override
    public Application getApp() {
        Application application = new Application();

        String appId = UUID.randomUUID().toString().replace("-", "");
        String apiKey = UUID.randomUUID().toString().replace("-", "");
        String masterKey = UUID.randomUUID().toString().replace("-", "");

        application.setAppId(appId);
        application.setApiKey(BCrypt.hashpw(apiKey, BCrypt.gensalt()));
        application.setMasterKey(BCrypt.hashpw(masterKey, BCrypt.gensalt()));

        final EntityId id = applicationService.create(application);
        if (id != null) {
            //Application app =  applicationService.read(id.toString());
            if (application != null) {
                application.setAppId(appId);
                application.setApiKey(apiKey);
                application.setMasterKey(masterKey);
                setStatus(Status.SUCCESS_OK);
                return application;
            }
        } else {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

}
