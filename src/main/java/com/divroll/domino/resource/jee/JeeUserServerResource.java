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

import com.alibaba.fastjson.JSONArray;
import com.divroll.domino.Constants;
import com.divroll.domino.model.Application;
import com.divroll.domino.model.User;
import com.divroll.domino.repository.UserRepository;
import com.divroll.domino.resource.UserResource;
import com.divroll.domino.service.WebTokenService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import scala.actors.threadpool.Arrays;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeUserServerResource extends BaseServerResource implements
        UserResource {

    private static final Logger LOG
            = Logger.getLogger(JeeUserServerResource.class.getName());


    @Inject
    @Named("defaultUserStore")
    String storeName;

    @Inject
    UserRepository userRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public User getUser() { // login
        if (!isAuthorized(appId, apiKey, masterKey)) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }
        String username = getQueryValue(Constants.QUERY_USERNAME);
        String password = getQueryValue(Constants.QUERY_PASSWORD);

        if(userId != null) {
            User userEntity = userRepository.getUser(appId, storeName, userId);
            if (userEntity == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }
            userEntity.setPassword(null);
            setStatus(Status.SUCCESS_OK);
            return userEntity;
        } else { // login
            User userEntity = userRepository.getUserByUsername(appId, storeName, username);
            String userId = userEntity.getEntityId();
            String existingPassword = userEntity.getPassword();


            if (userEntity == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }

            if (BCrypt.checkpw(password, existingPassword)) {
                Application app = applicationService.read(appId);
                if (app != null) {
                    String webToken = webTokenService.createToken(app.getMasterKey(), userId);
                    User user = new User();
                    user.setEntityId(userId);
                    user.setWebToken(webToken);
                    userEntity.setPassword(null);
                    setStatus(Status.SUCCESS_OK);
                    return user;
                }
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
        }

        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return null;
    }

    @Override
    public User updateUser(User entity) {
        Representation representation = returnNull();
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }

            Boolean isMaster =isMaster(appId, masterKey);

            if (!isMaster && (authToken == null || authToken.isEmpty()) ) {
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
            publicRead = entity.getPublicRead() != null ? entity.getPublicRead() : true;
            publicWrite = entity.getPublicWrite() != null ? entity.getPublicWrite() : true;

            if (newUsername == null || newPlainPassword == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                return null;

            }

            String[] read = new String[]{};
            String[] write = new String[]{};

            if (aclRead != null) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclRead);
                    List<String> aclReadList = new LinkedList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        aclReadList.add(jsonArray.getString(i));
                    }
                    read = aclReadList.toArray(new String[aclReadList.size()]);
                } catch (Exception e) {
                    // do nothing
                }
            }

            if (aclWrite != null) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclWrite);
                    List<String> aclWriteList = new LinkedList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        aclWriteList.add(jsonArray.getString(i));
                    }
                    write = aclWriteList.toArray(new String[aclWriteList.size()]);
                } catch (Exception e) {
                    // do nothing
                }
            }
            if (isMaster) {
                String newHashPassword = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());
                Boolean success = userRepository.updateUser(appId, storeName, userId, newUsername, newHashPassword, read, write, publicRead, publicWrite);
                if (success) {
                    String webToken = webTokenService.createToken(app.getMasterKey(), userId);
                    User user = new User();
                    user.setEntityId(entityId);
                    user.setPassword(null);
                    user.setAclRead(Arrays.asList(read));
                    user.setAclWrite(Arrays.asList(write));
                    user.setPublicRead(publicRead);
                    user.setPublicWrite(publicWrite);
                    user.setWebToken(webToken);
                    setStatus(Status.SUCCESS_OK);
                    return user;
                } else {
                    setStatus(Status.SERVER_ERROR_INTERNAL);
                }
            } else {
                String authUserID = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                if(authUserID != null) {
                    final User user = userRepository.getUser(appId, storeName, authUserID);
                    if (authUserID.equals(user.getEntityId())) {
                        String newHashPassword = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());
                        Boolean success = userRepository.updateUser(appId, storeName, authUserID, userId, newHashPassword, read, write, publicRead, publicWrite);
                        if (success) {
                            String webToken = webTokenService.createToken(app.getMasterKey(), userId);
                            user.setPassword(null);
                            user.setWebToken(webToken);
                            user.setPublicRead(publicRead);
                            user.setPublicWrite(publicWrite);
                            setStatus(Status.SUCCESS_OK);
                            return user;
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
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public void deleteUser(User entity) {
        try {
            if (appId == null || masterKey == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_MASTERKEY_MISSING);
                return;
            }
            if (!isMaster(appId, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, Constants.ERROR_MASTERKEY_INVALID);
                return;
            }
            if (userId == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_MISSING_USER_ID);
                return;
            }
            if (username == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_QUERY_USERNAME_REQUIRED);
                return;
            }

            User userEntity = userRepository.getUserByUsername(appId, storeName, username);
            String id = userEntity.getEntityId();
            if (userRepository.deleteUser(appId, storeName, id.toString())) {
                setStatus(Status.SUCCESS_OK);
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_CANNOT_DELETE_USER);
            }

        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return;
    }
}
