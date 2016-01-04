package com.divroll.webdash.server.resource;

import com.divroll.webdash.client.shared.Blog;
import org.restlet.resource.Get;

/**
 * Created by Kerby on 1/5/2016.
 */
public interface BlogResource {
    @Get
    Blog getBlog();
}
