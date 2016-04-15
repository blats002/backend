package com.divroll.webdash.server.resource;

import com.divroll.webdash.shared.Subdomain;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

public interface SubdomainResource {
    @Get
    public Subdomain getSubdomain();
    @Put
    public Subdomain updateSubdomain(Subdomain subdomain);
    @Delete
    public void deleteSubdomain(Subdomain subdomain);
}
