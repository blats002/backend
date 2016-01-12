package com.divroll.webdash.server.repository;

import com.divroll.webdash.client.shared.Subdomain;
import com.divroll.webdash.client.shared.Subdomains;

public interface SubdomainRepository extends CrudRepository<Subdomain> {
    public Subdomains findByUser(Long id);
}
