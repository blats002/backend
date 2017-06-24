package com..bucket.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Post;

public interface SSLResource {
    @Post
    Representation post(Representation entity);
}
