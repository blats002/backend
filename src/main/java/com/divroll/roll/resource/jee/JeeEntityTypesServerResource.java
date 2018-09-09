package com.divroll.roll.resource.jee;

import com.divroll.roll.model.EntityTypes;
import com.divroll.roll.resource.EntityTypesResource;
import com.divroll.roll.xodus.XodusStore;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Status;

public class JeeEntityTypesServerResource extends BaseServerResource
    implements EntityTypesResource {

    @Inject
    XodusStore store;

    @Override
    public EntityTypes getEntityTypes() {
        EntityTypes entityTypes = new EntityTypes();
        if(appId == null || appId.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing App ID");
            return null;
        }
        if(isMaster(appId, masterKey)) {
            entityTypes.setLimit(0);
            entityTypes.setSkip(0);
            entityTypes.setResults(store.listEntityTypes(appId));
        } else {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }
        return entityTypes;
    }
}
