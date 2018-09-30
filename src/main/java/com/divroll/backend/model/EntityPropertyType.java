package com.divroll.backend.model;

public class EntityPropertyType {

    public enum TYPE { OBJECT, ARRAY, BOOLEAN, STRING, NUMBER }

    private String propertyName;
    private TYPE propertyType;

    public EntityPropertyType(String propertyName, TYPE propertyType) {
        setPropertyName(propertyName);
        setPropertyType(propertyType);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public TYPE getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(TYPE propertyType) {
        this.propertyType = propertyType;
    }
}
