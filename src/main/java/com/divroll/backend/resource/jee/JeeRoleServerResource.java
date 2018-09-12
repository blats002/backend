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
import com.divroll.backend.model.Application;
import com.divroll.backend.model.EntityStub;
import com.divroll.backend.model.Role;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.resource.RoleResource;
import com.divroll.backend.service.WebTokenService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException;
import org.restlet.data.Status;

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
            if (!validateId(roleId)) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, Constants.ERROR_MISSING_ROLE_ID);
                return null;
            }
            Application app = applicationService.read(appId);
            if (app == null) {
                return null;
            }
            if (isMaster(appId, masterKey)) {
                Role role = roleRepository.getRole(appId, storeName, roleId);
                if (role != null) {
                    setStatus(Status.SUCCESS_OK);
                    return role;
                } else {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            } else {

                String authUserId = null;
                if (authToken != null) {
                    authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                }

                Boolean isAccess = false;

                Role role = roleRepository.getRole(appId, storeName, roleId);
                if(role == null) {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return null;
                }
                Boolean publicRead = role.getPublicRead();
                if (authUserId != null && role.getAclRead().contains(authUserId)) {
                    isAccess = true;
                }
                if (!publicRead && !isAccess) {
                    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                    return null;
                }
                if (role != null) {
                    setStatus(Status.SUCCESS_OK);
                    return role;
                } else {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            }

        } catch (EntityRemovedInDatabaseException e) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, Constants.ERROR_ENTITY_WAS_REMOVED);
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
            if (!validateId(roleId)) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                return null;
            }

            String[] read = new String[]{};
            String[] write = new String[]{};

            List<EntityStub> aclReadList = entity.getAclRead();
            List<EntityStub> aclWriteList = entity.getAclWrite();

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

            validateIds(read, write);

            String newRoleName = entity.getName();
            publicRead = entity.getPublicRead() != null ? entity.getPublicRead() : true;
            publicWrite = entity.getPublicWrite() != null ? entity.getPublicWrite() : true;

            if (!isMaster(appId, masterKey)) {
                Role role = roleRepository.getRole(appId, storeName, roleId);
                String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                if (role == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                } else {
                    if (role.getPublicWrite() || role.getAclWrite().contains(authUserId)) {
                        Boolean success = roleRepository.updateRole(appId, storeName, roleId, newRoleName, read, write, publicRead, publicWrite);
                        if (success) {
                            setStatus(Status.SUCCESS_CREATED);
                            role.setPublicWrite(publicWrite);
                            role.setPublicRead(publicRead);
                            role.setName(newRoleName);
                            return role;
                        } else {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        }
                    } else {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                    }
                }
            } else {
                Boolean success = roleRepository.updateRole(appId, storeName, roleId, newRoleName, read, write, publicRead, publicWrite);
                if (success) {
                    setStatus(Status.SUCCESS_OK);
                    Role role = new Role();
                    role.setName(newRoleName);
                    role.setEntityId(roleId);
                    role.setAclWrite(ACLHelper.convert(write));
                    role.setAclRead(ACLHelper.convert(read));
                    role.setPublicRead(publicRead);
                    role.setPublicWrite(publicWrite);
                    return role;
                } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            }

        } catch (IllegalArgumentException e) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
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
            if (roleId == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            Application app = applicationService.read(appId);
            if (app == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }
            if (!isMaster(appId, masterKey)) {
                Role role = roleRepository.getRole(appId, storeName, roleId);
                String authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
                if (role == null) {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                } else {
                    if (role.getPublicWrite() || ACLHelper.contains(authUserId, role.getAclWrite())) {
                        Boolean success = roleRepository.deleteRole(appId, storeName, roleId);
                        if (success) {
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
                if (success) {
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
