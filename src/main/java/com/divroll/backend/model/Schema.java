package com.divroll.backend.model;

public class Schema {
    private String entityType;
    private SchemaPropertyList schemaProperties;

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public SchemaPropertyList getSchemaProperties() {
        if(schemaProperties == null) {
            schemaProperties = new SchemaPropertyList();
        }
        return schemaProperties;
    }

    public void setSchemaProperties(SchemaPropertyList schemaProperties) {
        this.schemaProperties = schemaProperties;
    }

    public SchemaProperty get(String propertyName) {
        final SchemaProperty[] result = {null};
        schemaProperties.forEach(schemaProperty -> {
            if(schemaProperty.getPropertyName().equals(propertyName)) {
                result[0] = schemaProperty;
            }
        });
        return result[0];
    }

}
