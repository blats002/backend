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
import com.alibaba.fastjson.JSONObject;
import com.divroll.domino.Constants;
import com.divroll.domino.model.Application;
import com.divroll.domino.model.User;
import com.divroll.domino.model.Users;
import com.divroll.domino.repository.UserRepository;
import com.divroll.domino.resource.UsersReource;
import com.divroll.domino.service.WebTokenService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeUsersServerReource extends BaseServerResource
    implements UsersReource {

    @Inject
    @Named("defaultUserStore")
    String storeName;

    @Inject
    UserRepository userRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Users getUsers() {
        String skip = getQueryValue(Constants.QUERY_SKIP);
        String limit = getQueryValue(Constants.QUERY_LIMIT);
        if(!isMaster(appId, masterKey)) {

        } else {
            List<User> results = userRepository.listUsers(appId, storeName, Long.valueOf(skip), Long.valueOf(limit));
            Users users = new Users();
            users.setResults(results);
            users.setLimit(Long.valueOf(limit));
            users.setSkip(Long.valueOf(skip));
            setStatus(Status.SUCCESS_OK);
            return users;
        }
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

            String[] read = new String[]{Constants.ACL_ASTERISK};
            String[] write = new String[]{Constants.ACL_ASTERISK};

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

            String username = entity.getUsername();
            String plainPassword = entity.getPassword();

            User userEntity = userRepository.getUserByUsername(appId, storeName, username);

            if (userEntity != null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_USERNAME_EXISTS);
                return null;
            } else {
                Application app = applicationService.read(appId);
                if (app != null) {
                    String hashPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
                    String entityId = userRepository.createUser(appId, storeName, username, hashPassword, read, write);
                    if (entityId != null) {
                        String webToken = webTokenService.createToken(app.getMasterKey(), entityId);
                        JSONObject result = new JSONObject();
                        result.put(Constants.WEBTOKEN, webToken);
                        setStatus(Status.SUCCESS_CREATED);
                        User user = new User();
                        user.setEntityId(entityId.toString());
                        user.setUsername(username);
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


}
