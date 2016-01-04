package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.client.shared.Blogs;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.BlogResource;
import com.divroll.webdash.server.resource.BlogsResource;
import com.divroll.webdash.server.service.BlogService;
import com.divroll.webdash.server.service.WebTokenService;
import com.google.inject.Inject;

import java.util.logging.Logger;

public class GaeBlogsServerResource extends SelfInjectingServerResource
        implements BlogsResource {

    private static final Logger LOG
            = Logger.getLogger(GaeBlogsServerResource.class.getName());

    @Inject
    WebTokenService webTokenService;

    @Inject
    BlogService blogService;

    @Override
    public Blogs list() {
        return null;
    }
}
