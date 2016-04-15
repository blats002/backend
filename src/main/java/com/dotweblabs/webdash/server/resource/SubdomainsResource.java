package com.divroll.webdash.server.resource;

import com.divroll.webdash.client.shared.Subdomain;
import com.divroll.webdash.client.shared.Subdomains;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

public interface SubdomainsResource {
    @Get
    Subdomains list();
    @Post
    Subdomain createSubdomain(Subdomain subdomain);
}
