package com..bucket.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public interface WebsiteUploadResource {
    @Post
    Representation post(Representation entity);
}
