package com.divroll.roll.resource.jee;

import com.divroll.roll.Constants;
import com.divroll.roll.helper.ACLHelper;
import com.divroll.roll.model.Application;
import com.divroll.roll.model.EntityStub;
import com.divroll.roll.model.Role;
import com.divroll.roll.repository.EntityRepository;
import com.divroll.roll.repository.RoleRepository;
import com.divroll.roll.resource.LinkResource;
import com.divroll.roll.service.WebTokenService;
import com.google.inject.Inject;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JeeLinkServerResource extends BaseServerResource
    implements LinkResource {

    @Inject
    EntityRepository entityRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public void createLink(Representation entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }

            String authUserId = null;

            boolean isWriteAccess = false;
            boolean isMaster = false;
            boolean isPublic = false;

            try {
                authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            } catch (Exception e) {
                // do nothing
            }

            Map<String, Object> map = entityRepository.getEntity(appId, entityType, entityId);
            List<EntityStub> aclWriteList = map.get(Constants.ACL_WRITE) != null
                    ? (List<EntityStub>) map.get(Constants.ACL_WRITE) : new LinkedList<>();

            if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
                isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
            }

            if (isMaster(appId, masterKey)) {
                isMaster = true;
            } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
                isWriteAccess = true;
            } else if (authUserId != null) {
                List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                for (Role role : roles) {
                    if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
                        isWriteAccess = true;
                    }
                }
            }

            if (isMaster || isWriteAccess || isPublic) {
                if(entityRepository.linkEntity(appId, entityType, linkName,
                        entityId, targetEntityId)) {
                    setStatus(Status.SUCCESS_CREATED);
                } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }

        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public void deleteLink(Representation entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }

            String authUserId = null;

            boolean isWriteAccess = false;
            boolean isMaster = false;
            boolean isPublic = false;

            try {
                authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            } catch (Exception e) {
                // do nothing
            }

            Map<String, Object> map = entityRepository.getEntity(appId, entityType, entityId);

            if(map == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                List<EntityStub> aclWriteList = map.get(Constants.ACL_WRITE) != null
                        ? (List<EntityStub>) map.get(Constants.ACL_WRITE) : new LinkedList<>();

                if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
                    isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
                }

                if (isMaster(appId, masterKey)) {
                    isMaster = true;
                } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
                    isWriteAccess = true;
                } else if (authUserId != null) {
                    List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                    for (Role role : roles) {
                        if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
                            isWriteAccess = true;
                        }
                    }
                }

                if (isMaster || isWriteAccess || isPublic) {
                    if(entityRepository.unlinkEntity(appId, entityType, linkName,
                            entityId, targetEntityId)) {
                        setStatus(Status.SUCCESS_OK);
                    } else {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                } else {
                    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                }
            }

        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public Representation checkLink(Representation entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entity == null || entity.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            Application app = applicationService.read(appId);
            if (app == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }

            String authUserId = null;

            boolean isWriteAccess = false;
            boolean isMaster = false;
            boolean isPublic = false;

            try {
                authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
            } catch (Exception e) {
                // do nothing
            }

            Map<String, Object> map = entityRepository.getEntity(appId, entityType, entityId);

            if(map != null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);

            } else {
                List<EntityStub> aclWriteList = map.get(Constants.ACL_WRITE) != null
                        ? (List<EntityStub>) map.get(Constants.ACL_WRITE) : new LinkedList<>();

                if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
                    isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
                }

                if (isMaster(appId, masterKey)) {
                    isMaster = true;
                } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
                    isWriteAccess = true;
                } else if (authUserId != null) {
                    List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                    for (Role role : roles) {
                        if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
                            isWriteAccess = true;
                        }
                    }
                }

                if (isMaster || isWriteAccess || isPublic) {
                    if(entityRepository.isLinked(appId, entityType, linkName,
                            entityId, targetEntityId)) {
                        setStatus(Status.SUCCESS_OK);
                    } else {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    }
                } else {
                    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                }
            }



        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }
}
