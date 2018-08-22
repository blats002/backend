package com.divroll.domino.resource.jee;

import com.divroll.domino.Constants;
import com.divroll.domino.model.Application;
import com.divroll.domino.model.Role;
import com.divroll.domino.repository.EntityRepository;
import com.divroll.domino.repository.RoleRepository;
import com.divroll.domino.resource.LinksResource;
import com.divroll.domino.service.WebTokenService;
import com.google.inject.Inject;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JeeLinksServerResource extends BaseServerResource
    implements LinksResource {

    @Inject
    EntityRepository entityRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Representation getLinks(Representation entity) {
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
                List<String> aclWriteList = map.get(Constants.ACL_WRITE) != null
                        ? (List<String>) map.get(Constants.ACL_WRITE) : new LinkedList<>();

                if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
                    isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
                }

                if (isMaster(appId, masterKey)) {
                    isMaster = true;
                } else if (authUserId != null && aclWriteList.contains(authUserId)) {
                    isWriteAccess = true;
                } else if (authUserId != null) {
                    List<Role> roles = roleRepository.getRolesOfEntity(appId, authUserId);
                    for (Role role : roles) {
                        if (aclWriteList.contains(role.getEntityId())) {
                            isWriteAccess = true;
                        }
                    }
                }

                if (isMaster || isWriteAccess || isPublic) {
                    List<Map<String,Object>> entities = entityRepository.getLinkedEntities(appId, entityType, entityId, linkName);
                    if(entities != null) {
                        JSONObject responseBody = new JSONObject();
                        JSONObject entitiesJSONObject = new JSONObject();
                        entitiesJSONObject.put("results", entities);
                        responseBody.put("entities", entitiesJSONObject);
                        Representation representation = new JsonRepresentation(responseBody);
                        setStatus(Status.SUCCESS_OK);
                        return representation;
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
