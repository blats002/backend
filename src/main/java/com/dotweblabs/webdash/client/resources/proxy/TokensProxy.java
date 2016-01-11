package com.divroll.webdash.client.resources.proxy;

import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Result;

public interface TokensProxy extends ClientProxy {
    @Get
    public void signin(Result<String> url);
}
