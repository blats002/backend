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
import com.divroll.backend.helper.DTOHelper;
import com.divroll.backend.helper.ObjectLogger;
import com.divroll.backend.model.*;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.UsersResource;
import com.divroll.backend.service.WebTokenService;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;
import scala.actors.threadpool.Arrays;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeUsersServerResource extends BaseServerResource
        implements UsersResource {


    @Inject
    @Named("defaultUserStore")
    String storeName;

    @Inject
    UserRepository userRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Users listUsers() {

        int skipValue = 0;
        int limitValue = DEFAULT_LIMIT;

        if (skip != null && limit != null) {
            skipValue = skip;
            limitValue = limit;
        }

        Application app = applicationService.read(appId);
        if (app == null) {
            return null;
        }

        if (!isMaster()) {
            String authUserId = null;
            try {
                authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            } catch (Exception e) {
                // do nothing
            }
            List<User> processedResults = new LinkedList<>();
            List<User> results = userRepository.listUsers(appId, storeName, authUserId,
                    skipValue, limitValue, sort, false);
            Users users = new Users();
            users.setResults(DTOHelper.convert(results));
            users.setLimit(Long.valueOf(limitValue));
            users.setSkip(Long.valueOf(skipValue));
            setStatus(Status.SUCCESS_OK);
            return users;
        } else {
            List<User> results = userRepository
                    .listUsers(appId, storeName, null, skipValue, limitValue, null, true);
            Users users = new Users();
            users.setResults(DTOHelper.convert(results));
            users.setLimit(Long.valueOf(skipValue));
            users.setSkip(Long.valueOf(limitValue));
            setStatus(Status.SUCCESS_OK);
            return users;
        }
    }

    @Override
    public UserDTO createUser(UserDTO entity) {
        try {
            if (!isAuthorized()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entity == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            String[] read = new String[]{};
            String[] write = new String[]{};

            List<EntityStub> aclReadList = entity.getAclRead();
            List<EntityStub> aclWriteList = entity.getAclWrite();

            if (aclReadList == null) {
                aclReadList = new LinkedList<>();
            }

            if (aclWriteList == null) {
                aclWriteList = new LinkedList<>();
            }

            if ( (aclReadList == null || aclReadList.isEmpty()) && aclRead != null) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(aclRead);
                    if(jsonArray != null) {
                        if(!ACLHelper.validate(jsonArray)) {
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
                    if(jsonArray != null) {
                        if(!ACLHelper.validate(jsonArray)) {
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

            String username = entity.getUsername();
            String plainPassword = entity.getPassword();
            publicRead = entity.getPublicRead() != null ? entity.getPublicRead() : true;
            publicWrite = entity.getPublicWrite() != null ? entity.getPublicWrite() : true;

            List<RoleDTO> roles = entity.getRoles();
            String[] roleArray = DTOHelper.roleIdsOnly(roles);

            User userEntity = userRepository.getUserByUsername(appId, storeName, username);

            if (userEntity != null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_USERNAME_EXISTS);
                return null;
            } else {
                Application app = applicationService.read(appId);
                if (app != null) {
                    String hashPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                    validateIds(read, write);
                    String entityId = userRepository.createUser(appId, storeName, username, hashPassword, read, write,
                            publicRead, publicWrite, roleArray);
                    if (entityId != null) {
                        String webToken = webTokenService.createToken(app.getMasterKey(), entityId);
                        User user = new User();
                        user.setEntityId(entityId);
                        user.setUsername(username);
                        user.setWebToken(webToken);
                        user.setPublicRead(publicRead);
                        user.setPublicWrite(publicWrite);
                        user.setAclWrite(ACLHelper.convert(write));
                        user.setAclRead(ACLHelper.convert(read));
                        for (Object roleId : Arrays.asList(roleArray)) {
                            user.getRoles().add(new Role((String) roleId));
                        }
                        setStatus(Status.SUCCESS_CREATED);
                        return UserDTO.convert((User) ObjectLogger.log(user));
                    } else {
                        setStatus(Status.SERVER_ERROR_INTERNAL);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }


}
