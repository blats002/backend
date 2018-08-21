package com.divroll.domino.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

public interface BlobResource {
    @Post
    void setBlob(Representation entity);
    @Put
    void updateBlob(Representation entity);
    @Delete
    void deleteBlob(Representation entity);
    @Get
    Representation getBlob(Representation entity);
}
