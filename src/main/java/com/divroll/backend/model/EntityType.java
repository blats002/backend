package com.divroll.backend.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("entityType")
public class EntityType {
    private String entityType;
    @XStreamImplicit(itemFieldName = "propertyTypes")
    private List<EntityPropertyType> propertyTypes;

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public List<EntityPropertyType> getPropertyTypes() {
        return propertyTypes;
    }

    public void setPropertyTypes(List<EntityPropertyType> propertyTypes) {
        this.propertyTypes = propertyTypes;
    }
}
