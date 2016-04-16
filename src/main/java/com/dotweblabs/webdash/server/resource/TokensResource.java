package com.divroll.webdash.server.resource;

import com.divroll.webdash.shared.Token;
import org.restlet.resource.Get;

public interface TokensResource {
    @Get
    Token signin();
}
