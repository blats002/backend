package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.shared.Blog;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.BlogResource;
import com.divroll.webdash.server.service.BlogService;
import com.divroll.webdash.server.service.WebTokenService;
import com.google.inject.Inject;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeBlogServerResource extends SelfInjectingServerResource
        implements BlogResource {

    private static final Logger LOG
            = Logger.getLogger(GaeBlogServerResource.class.getName());

    @Inject
    WebTokenService webTokenService;

    @Inject
    BlogService blogService;

    @Override
    public Blog getBlog() {
        return null;
    }
}
