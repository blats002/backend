package com.divroll.backend.resource.jee;

import com.divroll.backend.model.Keys;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.resource.PropertyResource;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

public class JeePropertyServerResource extends BaseServerResource
    implements PropertyResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeePropertyServerResource.class);

    @Inject
    EntityRepository entityRepository;

    @Override
    public void deleteProperty(Representation representation) {
        if(propertyName == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        if(Keys.isReservedPropertyKey(propertyName)) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        if(isMaster()) {
            boolean isDeleted = entityRepository.deleteProperty(appId, entityType, propertyName);
            if(isDeleted) {
                setStatus(Status.SUCCESS_OK);
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } else {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
    }
}
