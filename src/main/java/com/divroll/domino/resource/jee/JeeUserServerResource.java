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

import com.alibaba.fastjson.JSONObject;
import com.divroll.domino.model.Application;
import com.divroll.domino.model.User;
import com.divroll.domino.resource.UserResource;
import com.divroll.domino.service.WebTokenService;
import com.divroll.domino.xodus.XodusEnvStore;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeUserServerResource extends BaseServerResource implements
        UserResource {

    private static final String KEY_SPACE = ":";

    @Inject
    @Named("defaultUserStore")
    String storeName;

    @Inject
    XodusEnvStore store;

    @Inject
    WebTokenService webTokenService;

    @Override
    public User getUser() {
        if (!isAuthorized(appId, apiKey, masterKey)) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }
        String username = getQueryValue("username");
        String password = getQueryValue("password");

        String uuidKey = "username:" + username + ":uuid";
        String existingUUID = store.get(appId, storeName, uuidKey, String.class);
        String usernameKey = "uuid:" + existingUUID + ":username";
        String passwordKey = "uuid:" + existingUUID + ":username:" + username + ":password";

        String existingUsername = store.get(appId, storeName, usernameKey, String.class);
        String existingPassword = store.get(appId, storeName, passwordKey, String.class);

        if (existingUsername == null || existingPassword == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        if (BCrypt.checkpw(password, existingPassword)) {
            Application app = applicationService.read(appId);
            if (app != null) {
                String webToken = webTokenService.createToken(app.getMasterKey(), existingUsername);
                JSONObject result = new JSONObject();
                result.put("webToken", webToken);
                setStatus(Status.SUCCESS_OK);
                User user = new User();
                user.setWebToken(webToken);
                return user;
            }
        } else {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return null;
    }

    @Override
    public User createUser(User entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entity == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            String username = entity.getUsername();
            String plainPassword = entity.getPassword();
            String uuid = UUID.randomUUID().toString().replace("-", "");

            String usernameKey = "uuid:" + uuid + ":username";
            String passwordKey = "uuid:" + uuid + ":username:" + username + ":password";
            String uuidKey = "username:" + username + ":uuid";

            String existingUUID = store.get(appId, storeName, uuidKey, String.class);
            if (existingUUID != null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Username already exists");
                return null;
            } else {
                Application app = applicationService.read(appId);
                if (app != null) {
                    Map<String, String> properties = new LinkedHashMap<>();
                    properties.put(usernameKey, username);
                    properties.put(passwordKey, BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
                    properties.put(uuidKey, uuid);

                    Boolean success = store.batchPut(appId, storeName, properties);
                    if (success) {
                        String webToken = webTokenService.createToken(app.getMasterKey(), username);
                        JSONObject result = new JSONObject();
                        result.put("webToken", webToken);
                        setStatus(Status.SUCCESS_CREATED);
                        User user = new User();
                        user.setWebToken(webToken);
                        return user;
                    } else {
                        setStatus(Status.SERVER_ERROR_INTERNAL);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public User updateUser(User entity) {
        Representation representation = returnNull();
        String username = getQueryValue("username");
        String password = getQueryValue("password");
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (username == null || password == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
                //return missingUsernamePasswordPair();
            }
            if (authToken == null || authToken.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
                //return returnMissingAuthToken();
            }
            if (entity == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            String newUsername = entity.getUsername();
            String newPlainPassword = entity.getPassword();

            if (newUsername == null || newPlainPassword == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                return null;

            }
            String authUsername = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);

            if (authUsername.equals(username)) {

                String uuidKey = "username:" + authUsername + ":uuid";

                String existingUUID = store.get(appId, storeName, uuidKey, String.class);

                String usernameKey = "uuid:" + existingUUID + ":username";
                String passwordKey = "uuid:" + existingUUID + ":username:" + authUsername + ":password";

                String existingUsername = store.get(appId, storeName, usernameKey, String.class);
                String existingPassword = store.get(appId, storeName, passwordKey, String.class);

                if ((existingUsername != null && existingUsername.equals(authUsername))
                        && (BCrypt.checkpw(password, existingPassword))) {

                    String newUUIDKey = "username:" + newUsername + ":uuid";
                    String newUsernameKey = "uuid:" + existingUUID + ":username";
                    String newPasswordKey = "uuid:" + existingUUID + ":username:" + newUsername + ":password";

                    String newHashPassword = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());

                    Map<String, String> properties = new LinkedHashMap<>();
                    properties.put(newUsernameKey, newUsername);
                    properties.put(newPasswordKey, newHashPassword);
                    properties.put(newUUIDKey, existingUUID);

                    Boolean success = store.batchPutDelete(appId, storeName, properties, uuidKey, usernameKey, passwordKey);
                    if (success) {
                        String webToken = webTokenService.createToken(app.getMasterKey(), newUsername);
                        User user = new User();
                        user.setWebToken(webToken);
                        setStatus(Status.SUCCESS_OK);
                    } else {
                        setStatus(Status.SERVER_ERROR_INTERNAL);
                    }
                } else {
                    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                    return null;
                }

            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public void deleteUser(User entity) {
        // TODO:
    }
}
