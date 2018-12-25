package com.divroll.backend.resource;

import com.divroll.backend.model.File;
import com.wordnik.swagger.annotations.Api;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public interface VirtualFileResource {
    @Post
    File createVirtualFile(Representation entity);

    @Delete
    void deleteVirtualFile(Representation entity);

    @Get
    Representation getVirtualFile(Representation entity);
}
