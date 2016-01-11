package com.divroll.webdash.server.resource;

import com.divroll.webdash.client.shared.User;
import org.restlet.resource.Get;

public interface TokensResource {
    @Get
    String signin();
}
