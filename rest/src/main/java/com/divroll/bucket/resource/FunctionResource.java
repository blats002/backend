package com.divroll.bucket.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

public interface FunctionResource {
    @Post
    Representation post(Representation entity);
    @Get
    Representation get(Representation entity);
    @Put
    Representation put(Representation entity);
    @Delete
    Representation delete(Representation entity);
}
