package com.divroll.backend.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Delete;

public interface PropertyResource {
    @Delete
    void deleteProperty(Representation representation);
}
