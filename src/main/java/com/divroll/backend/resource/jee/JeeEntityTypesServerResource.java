package com.divroll.backend.resource.jee;

import com.divroll.backend.model.EntityTypes;
import com.divroll.backend.resource.EntityTypesResource;
import com.divroll.backend.xodus.XodusStore;
import com.google.inject.Inject;
import org.restlet.data.Status;

public class JeeEntityTypesServerResource extends BaseServerResource
    implements EntityTypesResource {

    @Inject
    XodusStore store;

    @Override
    public EntityTypes getEntityTypes() {
        try {
            EntityTypes entityTypes = new EntityTypes();
            if(appId == null || appId.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing App ID");
                return null;
            }
            if(isMaster()) {
                entityTypes.setLimit(0);
                entityTypes.setSkip(0);
                entityTypes.setResults(store.listEntityTypes(appId));
                setStatus(Status.SUCCESS_OK);
                return entityTypes;
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }
}
