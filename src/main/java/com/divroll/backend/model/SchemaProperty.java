package com.divroll.backend.model;

public class SchemaProperty {

    public TYPE getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(TYPE propertyType) {
        this.propertyType = propertyType;
    }

    public enum TYPE { OBJECT, ARRAY, BOOLEAN, STRING, NUMBER }

    private String propertyName;
    private TYPE propertyType;

    public SchemaProperty() {}

    public SchemaProperty(String propertyName, TYPE propertyType) {
        setPropertyType(propertyType);
        setPropertyName(propertyName);
    }

    public SchemaProperty(String propertyName, String propertyType) {
        setPropertyName(propertyName);
        if(propertyType.equals(TYPE.OBJECT.toString())) {
            setPropertyType(TYPE.OBJECT);
        } else if(propertyType.equals(TYPE.ARRAY.toString())) {
            setPropertyType(TYPE.ARRAY);
        } else if(propertyType.equals(TYPE.BOOLEAN.toString())) {
            setPropertyType(TYPE.BOOLEAN);
        } else if(propertyType.equals(TYPE.STRING.toString())) {
            setPropertyType(TYPE.STRING);
        } else if(propertyType.equals(TYPE.NUMBER.toString())) {
            setPropertyType(TYPE.NUMBER);
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

}
