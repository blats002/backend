package com.divroll.domino.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public interface BackupResource {
    @Post
    void restore(Representation entity);
    @Get
    Representation backup(Representation entity);
}
