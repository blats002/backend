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
import com.divroll.domino.model.Application;
import com.divroll.domino.model.Role;
import com.divroll.domino.repository.RoleRepository;
import com.divroll.domino.resource.RoleResource;
import com.divroll.domino.service.WebTokenService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException;
import org.restlet.data.Status;
import scala.actors.threadpool.Arrays;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeRoleServerResource extends BaseServerResource
        implements RoleResource {

    @Inject
    @Named("defaultRoleStore")
    String storeName;

    @Inject
    RoleRepository roleRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Role getRole() {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if(roleId == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing role ID in request");
                return null;
            }
            Application app = applicationService.read(appId);
            if (app == null) {
                return null;
            }
            if(isMaster(appId, masterKey)) {
                Role role = roleRepository.getRole(appId, storeName, roleId);
                if(role != null) {
                    setStatus(Status.SUCCESS_OK);
                    return role;
                } else {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            } else {
                if(authToken == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Nissing Auth Token in request");
                    return null;
                }
                String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);

                Boolean publicRead = false;
                Boolean isAccess = false;

                if (aclRead != null) {
                    try {
                        JSONArray jsonArray = JSONArray.parseArray(aclRead);
                        List<String> aclReadList = new LinkedList<>();
                        for (int i = 0; i < jsonArray.size(); i++) {
                            aclReadList.add(jsonArray.getString(i));
                        }
                        if(aclReadList.contains("*")) {
                            publicRead = true;
                        } else if(aclReadList.contains(authUserId)) {
                            isAccess = true;
                        }
                        if(!publicRead && !isAccess) {
                            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                            return null;
                        }
                        Role role = roleRepository.getRole(appId, storeName, roleId);
                        if(role != null) {
                            setStatus(Status.SUCCESS_CREATED);
                            return role;
                        } else {
                            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        }
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            }

        } catch (EntityRemovedInDatabaseException e) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Entity was removed ");
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public Role updateRole(Role entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entity == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }
            if(roleId == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                return null;
            }

            String[] read = new String[]{"*"};
            String[] write = new String[]{"*"};

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
            String newRoleName = entity.getName();

            if(!isMaster(appId, masterKey)) {
                Role role = roleRepository.getRole(appId, storeName, roleId);
                String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                if(role == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                } else {
                    if(role.getPublicWrite() || role.getAclWrite().contains(authUserId)) {
                        Boolean success = roleRepository.updateRole(appId, storeName, roleId, newRoleName, read, write);
                        if(success) {
                            setStatus(Status.SUCCESS_CREATED);
                            role.setName(newRoleName);
                            if(read != null) {
                                role.setAclRead(Lists.newArrayList(read));
                                role.setPublicRead(Arrays.asList(read).contains("*"));
                            }
                            if(write != null) {
                                role.setAclWrite(Lists.newArrayList(write));
                                role.setPublicWrite(Arrays.asList(write).contains("*"));
                            }
                            return role;
                        } else {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        }
                    } else {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                    }
                }
            } else {
                Boolean success = roleRepository.updateRole(appId, storeName, roleId, newRoleName, read, write);
                if(success) {
                    setStatus(Status.SUCCESS_CREATED);
                    Role role = new Role();
                    role.setName(newRoleName);
                    role.setEntityId(roleId);
                    return role;
                } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public void deleteRole(Role entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            if(roleId == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            Application app = applicationService.read(appId);
            if (app == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }
            if(!isMaster(appId, masterKey)) {
                Role role = roleRepository.getRole(appId, storeName, roleId);
                String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                if(role == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                } else {
                    if(role.getPublicWrite() || role.getAclWrite().contains(authUserId)) {
                        Boolean success = roleRepository.deleteRole(appId, storeName, roleId);
                        if(success) {
                            setStatus(Status.SUCCESS_OK);
                        } else {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        }
                    } else {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                    }
                }
            } else {
                // Master key bypasses all checks
                Boolean success = roleRepository.deleteRole(appId, storeName, roleId);
                if(success) {
                    setStatus(Status.SUCCESS_OK);
                } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return;
    }

}
