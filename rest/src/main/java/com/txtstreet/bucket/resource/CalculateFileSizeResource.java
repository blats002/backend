package com..bucket.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public interface CalculateFileSizeResource {
    @Get
    Representation get(Representation entity);
}
