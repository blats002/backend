package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.client.shared.Subdomain;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.SubdomainResource;

public class GaeSubdomainServerResource extends SelfInjectingServerResource
    implements SubdomainResource {


    @Override
    public Subdomain getSubdomain() {
        return Subdomain.sample();
    }

    @Override
    public Subdomain updateSubdomain(Subdomain subdomain) {
        return Subdomain.sample();
    }

    @Override
    public void deleteSubdomain(Subdomain subdomain) {

    }
}
