package com.divroll.bucket.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public interface SSLResource {
    @Post
    Representation post(Representation entity);
    @Get
    Representation get(Representation entity);
}
