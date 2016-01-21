package com.divroll.webdash.client.resources.proxy;

import com.divroll.webdash.client.shared.Token;
import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Result;

public interface TokensProxy extends ClientProxy {
    @Get
    public void signin(Result<Token> token);
}
