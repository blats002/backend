package com.divroll.backend.model;

import java.util.LinkedList;

public class SchemaPropertyList extends LinkedList<SchemaProperty> {
    @Override
    public boolean contains(Object o) {
        final boolean[] contains = {false};
        if(o instanceof SchemaProperty) {
            SchemaProperty schemaProperty = (SchemaProperty) o;
            forEach(schemaProperty1 -> {
                if(schemaProperty1.getPropertyName().equals(schemaProperty.getPropertyName())) {
                    contains[0] = true;
                }
            });
        } else if(o instanceof String) {
            forEach(schemaProperty1 -> {
                if(o.equals(schemaProperty1.getPropertyName())) {
                    contains[0] = true;
                }
            });
        }
        return contains[0];
    }
}
