package com.divroll.backend.model;

import com.divroll.backend.Constants;

public class Keys {
    public static boolean isReservedPropertyKey(String propertyKey) {
        if(propertyKey.equals(Constants.RESERVED_FIELD_ENTITY_ID)) {
            return true;
        } else if(propertyKey.equals(Constants.RESERVED_FIELD_ACL_READ)) {
            return true;
        } else if(propertyKey.equals(Constants.RESERVED_FIELD_ACL_WRITE)) {
            return true;
        } else if(propertyKey.equals(Constants.RESERVED_FIELD_LINKS)) {
            return true;
        } else if(propertyKey.equals(Constants.RESERVED_FIELD_BLOBNAMES)) {
            return true;
        } else if(propertyKey.equals(Constants.RESERVED_FIELD_PUBLICREAD)) {
            return true;
        } else if(propertyKey.equals(Constants.RESERVED_FIELD_ACL_WRITE)) {
            return true;
        }
        return false;
    }
}
