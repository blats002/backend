package com.divroll.webdash.server.resource;

import com.divroll.webdash.client.shared.Token;
import com.divroll.webdash.client.shared.User;
import org.restlet.resource.Get;

public interface TokensResource {
    @Get
    Token signin();
}
