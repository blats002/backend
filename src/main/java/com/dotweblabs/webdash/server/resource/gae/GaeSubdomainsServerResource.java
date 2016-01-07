package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.client.shared.Subdomain;
import com.divroll.webdash.client.shared.Subdomains;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.SubdomainsResource;

public class GaeSubdomainsServerResource extends SelfInjectingServerResource
    implements SubdomainsResource {
    @Override
    public Subdomains list() {
        return null;
    }

    @Override
    public Subdomain createSubdomain(Subdomain subdomain) {
        return null;
    }
}
