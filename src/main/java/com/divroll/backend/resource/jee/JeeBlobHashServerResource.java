package com.divroll.backend.resource.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.helper.ACLHelper;
import com.divroll.backend.helper.JSON;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.EntityStub;
import com.divroll.backend.model.Role;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.resource.BlobHashResource;
import com.divroll.backend.service.PubSubService;
import com.divroll.backend.service.WebTokenService;
import com.divroll.backend.util.Base64;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.json.JSONObject;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JeeBlobHashServerResource extends BaseServerResource implements BlobHashResource {

    private static final Logger LOG = LoggerFactory.getLogger(JeeBlobHashServerResource.class);

    @Inject
    EntityRepository entityRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    WebTokenService webTokenService;

    @Inject
    PubSubService pubSubService;

    @Inject
    @Named("namespaceProperty")
    String namespaceProperty;

    @Override
    protected void doInit() {
        super.doInit();
        if(authToken == null || authToken.isEmpty()) {
            authToken = getQueryValue("authToken");
        }
        if(appId == null || appId.isEmpty()) {
            appId = getQueryValue("appId");
        }
        if(apiKey == null || apiKey.isEmpty()) {
            apiKey = getQueryValue("apiKey");
        }
        if(masterKey == null || masterKey.isEmpty()) {
            masterKey = getQueryValue("masterKey");
        }
        if(namespace == null || namespace.isEmpty()) {
            namespace = getQueryValue(namespaceProperty);
        }
        if(entityType == null || entityType.isEmpty()) {
            entityType = getQueryValue("entityType");
        }
        if(entityId == null || entityId.isEmpty()) {
            entityId = getQueryValue("entityId");
        }
        if(blobName == null || blobName.isEmpty()) {
            blobName = getQueryValue("blobName");
        }
        if(contentDisposition == null || contentDisposition.isEmpty()) {
            contentDisposition = getQueryValue("contentDisposition");
        }
    }

    @Override
    public Representation getBlob() {
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


            Map<String, Comparable> map =
                    entityRepository.getEntity(appId, namespace, entityType, entityId, null);
            List<EntityStub> aclWriteList =
                    map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
                            ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE)
                            : new LinkedList<>();

            if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
                isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
            }

            if (isMaster()) {
                isMaster = true;
            } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
                isWriteAccess = true;
            } else if (authUserId != null) {
                List<Role> roles = roleRepository.getRolesOfEntity(appId, namespace, authUserId);
                for (Role role : roles) {
                    if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
                        isWriteAccess = true;
                    }
                }
            }


            if (isMaster || isWriteAccess || isPublic) {
                Long count = entityRepository.countEntityBlobSize(appId, namespace, entityType, entityId, blobName);
                InputStream is =
                        entityRepository.getEntityBlob(appId, namespace, entityType, entityId, blobName);

                if (encoding != null && encoding.equals("base64")) {
                    if (is == null) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return null;
                    } else {
                        String base64 = BaseEncoding.base64().encode(ByteStreams.toByteArray(is));
                        Representation representation = new StringRepresentation(base64);
                        setStatus(Status.SUCCESS_OK);
                        return representation;
                    }
                } else {
                    if (is == null) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return null;
                    } else {
                        Representation representation = new InputRepresentation(is);
                        representation.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
                        representation.setSize(count);
                        if(contentDisposition != null && !contentDisposition.isEmpty()) {
                            if(contentDisposition.equals(Disposition.TYPE_ATTACHMENT)) {
                                representation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT));
                            } else if(contentDisposition.equals(Disposition.TYPE_INLINE)) {
                                representation.setDisposition(new Disposition(Disposition.TYPE_INLINE));
                            } else if(contentDisposition.equals(Disposition.TYPE_NONE)) {
                                representation.setDisposition(new Disposition(Disposition.TYPE_NONE));
                            }
                        }
                        setStatus(Status.SUCCESS_OK);
                        return representation;
                    }
                }
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }

        } catch (Exception e) {
            return internalError(stackTraceToString(e));
        }
        return null;
    }
}
