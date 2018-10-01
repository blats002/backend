package com.divroll.backend.resource;

import com.divroll.backend.model.File;
import org.restlet.representation.Representation;

public interface FileResource {
    File createFile(Representation entity);
    void deleteFile(Representation entity);
    Representation getFile(Representation entity);
}
