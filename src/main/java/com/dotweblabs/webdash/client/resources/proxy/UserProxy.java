package com.divroll.webdash.client.resources.proxy;

import com.divroll.webdash.client.shared.User;
import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Result;

public interface UserProxy extends ClientProxy {
    @Get
    public void getUser(Result<User> url);
}
