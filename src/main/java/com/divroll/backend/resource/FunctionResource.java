package com.divroll.backend.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

public interface FunctionResource {
    @Get
    Representation getMethod(Representation entity);
    @Post
    Representation postMethod(Representation entity);
    @Put
    Representation putMethod(Representation entity);
    @Delete
    Representation deleteMethod(Representation entity);
}
