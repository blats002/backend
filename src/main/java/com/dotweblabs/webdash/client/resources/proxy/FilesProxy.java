package com.divroll.webdash.client.resources.proxy;


import com.divroll.webdash.client.shared.Files;
import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Result;

public interface FilesProxy extends ClientProxy {
    @Get
    public void list(Result<Files> result);
}
