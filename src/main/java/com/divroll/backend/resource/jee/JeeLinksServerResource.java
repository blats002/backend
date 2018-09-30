package com.divroll.backend.resource.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.helper.ACLHelper;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.EntityStub;
import com.divroll.backend.model.Role;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.resource.LinksResource;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JeeLinksServerResource extends BaseServerResource
    implements LinksResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeLinksServerResource.class);

    @Inject
    EntityRepository entityRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Representation getLinks(Representation entity) {
        try {
            if (!isAuthorized()) {
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

            Map<String, Object> map = entityRepository.getEntity(appId, entityType, entityId);

            if(map == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                List<EntityStub> aclWriteList = map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
                        ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE) : new LinkedList<>();

                if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
                    isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
                }

                if (isMaster()) {
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
