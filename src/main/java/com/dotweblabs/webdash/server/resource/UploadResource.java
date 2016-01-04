package com.divroll.webdash.server.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Post;

public interface UploadResource {
    @Post
    public String upload(Representation entity) throws Exception;
}
