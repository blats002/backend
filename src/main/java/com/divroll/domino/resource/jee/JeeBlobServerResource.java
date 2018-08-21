package com.divroll.domino.resource.jee;

import com.divroll.domino.Constants;
import com.divroll.domino.model.Application;
import com.divroll.domino.model.Role;
import com.divroll.domino.repository.EntityRepository;
import com.divroll.domino.repository.RoleRepository;
import com.divroll.domino.resource.BlobResource;
import com.divroll.domino.service.WebTokenService;
import com.google.inject.Inject;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JeeBlobServerResource extends BaseServerResource
    implements BlobResource {

    @Inject
    EntityRepository entityRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public void setBlob(Representation entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            if (entity == null || entity.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
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

            Map<String,Object> map = entityRepository.getEntity(appId, entityType, entityId);
            List<String> aclWriteList = map.get(Constants.ACL_WRITE) != null
                    ? (List<String>) map.get(Constants.ACL_WRITE) : new LinkedList<>();

            if( map.get(Constants.RESERVED_FIELD_PUBLICWRITE)  != null) {
                isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
            }

            if(isMaster(appId, masterKey)) {
                isMaster = true;
            } else if(authUserId != null && aclWriteList.contains(authUserId)) {
                isWriteAccess = true;
            } else if(authUserId != null){
                List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                for(Role role : roles) {
                    if(aclWriteList.contains(role.getEntityId())) {
                        isWriteAccess = true;
                    }
                }
            }

            if( isMaster || isWriteAccess || isPublic) {
                // TODO: Compress stream
                if(entityRepository.createEntityBlob(appId, entityType, entityId, blobName,  entity.getStream())) {
                    setStatus(Status.SUCCESS_CREATED);
                } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }

        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public void updateBlob(Representation entity) {
        setBlob(entity);
    }

    @Override
    public void deleteBlob(Representation entity) {
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

            Map<String,Object> map = entityRepository.getEntity(appId, entityType, entityId);
            List<String> aclWriteList = map.get(Constants.ACL_WRITE) != null
                    ? (List<String>) map.get(Constants.ACL_WRITE) : new LinkedList<>();

            if( map.get(Constants.RESERVED_FIELD_PUBLICWRITE)  != null) {
                isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
            }

            if(isMaster(appId, masterKey)) {
                isMaster = true;
            } else if(authUserId != null && aclWriteList.contains(authUserId)) {
                isWriteAccess = true;
            } else if(authUserId != null){
                List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                for(Role role : roles) {
                    if(aclWriteList.contains(role.getEntityId())) {
                        isWriteAccess = true;
                    }
                }
            }

            if( isMaster || isWriteAccess || isPublic) {
                if(entityRepository.deleteEntityBlob(appId, entityType, entityId, blobName)) {
                    setStatus(Status.SUCCESS_OK);
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
    public Representation getBlob(Representation entity) {
        try {
            if (!isAuthorized(appId, apiKey, masterKey)) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
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

            Map<String,Object> map = entityRepository.getEntity(appId, entityType, entityId);
            List<String> aclWriteList = map.get(Constants.ACL_WRITE) != null
                    ? (List<String>) map.get(Constants.ACL_WRITE) : new LinkedList<>();

            if( map.get(Constants.RESERVED_FIELD_PUBLICWRITE)  != null) {
                isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
            }

            if(isMaster(appId, masterKey)) {
                isMaster = true;
            } else if(authUserId != null && aclWriteList.contains(authUserId)) {
                isWriteAccess = true;
            } else if(authUserId != null){
                List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                for(Role role : roles) {
                    if(aclWriteList.contains(role.getEntityId())) {
                        isWriteAccess = true;
                    }
                }
            }

            if( isMaster || isWriteAccess || isPublic) {
                InputStream is = entityRepository.getEntityBlob(appId, entityType, entityId, blobName);
                if(is == null) {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    return null;
                } else {
                    Representation representation = new InputRepresentation(is);
                    setStatus(Status.SUCCESS_OK);
                    return representation;
                }
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }

        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }
}
