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

import com.alibaba.fastjson.JSONObject;
import com.divroll.backend.Constants;
import com.divroll.backend.guice.SelfInjectingServerResource;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.repository.SuperuserRepository;
import com.divroll.backend.resource.SuperuserResource;
import com.divroll.backend.service.WebTokenService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

public class JeeSuperuserServerResource extends SelfInjectingServerResource
    implements SuperuserResource {

    @Inject
    SuperuserRepository superuserRepository;

    @Inject
    WebTokenService webTokenService;

    @Inject
    @Named("masterSecret")
    String masterSecret;

    @Override
    public Representation getUser() {

        String username = getQueryValue(Constants.QUERY_USERNAME);
        String password = getQueryValue(Constants.QUERY_PASSWORD);

        if(username == null || password == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }

        Superuser superuser = superuserRepository.getUserByUsername(username);
        if(superuser != null) {
            String superuserId = superuser.getEntityId();
            String existingPassword = superuser.getPassword();
            if (BCrypt.checkpw(password, existingPassword)) {
                String authToken = webTokenService.createToken(masterSecret, superuserId);
                superuser.setAuthToken(authToken);
                superuser.setPassword(null);
                superuser.setEntityId(superuserId);
                setStatus(Status.SUCCESS_OK);
                return new JsonRepresentation(asJSONObject(superuser));

            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
        } else {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
        return null;
    }

    @Override
    public Representation updateUser(Superuser entity) {
        return null;
    }

    @Override
    public void deleteUser(Superuser entity) {

    }

    public static JSONObject asJSONObject(Superuser userEntity) {
        JSONObject jsonObject = new JSONObject();
        JSONObject userObject = new JSONObject();
        userObject.put("entityId", userEntity.getEntityId());
        userObject.put("username", userEntity.getUsername());
        userObject.put("authToken", userEntity.getAuthToken());
//        userObject.put("dateCreated", userEntity.getDateCreated());
//        userObject.put("dateUpdated", userEntity.getDateUpdated());
        jsonObject.put("superuser", userObject);
        return jsonObject;
    }

}


