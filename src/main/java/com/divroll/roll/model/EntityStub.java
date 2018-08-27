package com.divroll.roll.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("entity")
public class EntityStub {
    private String entityId;
    private String entityType;

    private EntityStub() {}

    public EntityStub(String entityId) {
        setEntityId(entityId);
    }

    public EntityStub(String entityId, String entityType) {
        setEntityId(entityId);
        setEntityType(entityType);
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}
