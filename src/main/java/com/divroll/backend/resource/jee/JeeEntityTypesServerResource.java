package com.divroll.backend.resource.jee;

import com.divroll.backend.model.EntityType;
import com.divroll.backend.model.EntityTypes;
import com.divroll.backend.resource.EntityTypesResource;
import com.divroll.backend.xodus.XodusStore;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import org.restlet.data.Status;

import java.util.LinkedList;
import java.util.List;

public class JeeEntityTypesServerResource extends BaseServerResource
    implements EntityTypesResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeEntityTypesServerResource.class);

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

                List<EntityType> entityTypeList = new LinkedList<>();
                store.listEntityTypes(appId).forEach(s -> {
                    EntityType entityType = new EntityType();
                    entityType.setEntityType(s);
                    entityType.setPropertyTypes(store.listPropertyTypes(appId, s));
                    entityTypeList.add(entityType);
                });

                entityTypes.setResults(entityTypeList);
                setStatus(Status.SUCCESS_OK);
                return entityTypes;
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }
}
