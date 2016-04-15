package com.divroll.webdash.client.resources.proxy;

import com.divroll.webdash.client.shared.Subdomain;
import com.divroll.webdash.client.shared.Subdomains;
import org.restlet.client.resource.ClientProxy;
import org.restlet.client.resource.Get;
import org.restlet.client.resource.Post;
import org.restlet.client.resource.Result;

/**
 * Created by Kerby on 1/15/2016.
 */
public interface SubdomainsProxy extends ClientProxy {
    @Post
    public void save(Subdomain subdomain, Result<Subdomain> result);
    @Get
    public void list(Result<Subdomains> result);
}
