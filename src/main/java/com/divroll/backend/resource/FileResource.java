package com.divroll.backend.resource;

import com.divroll.backend.model.File;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public interface FileResource {
    @Post
    File createFile(Representation entity);
    @Delete
    void deleteFile(Representation entity);
    @Get
    Representation getFile(Representation entity);
}
