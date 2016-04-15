package com.divroll.webdash.server.repository;

import com.divroll.webdash.shared.Subdomain;
import com.divroll.webdash.shared.Subdomains;

public interface SubdomainRepository extends CrudRepository<Subdomain> {
    public Subdomains findByUser(Long id);
}
