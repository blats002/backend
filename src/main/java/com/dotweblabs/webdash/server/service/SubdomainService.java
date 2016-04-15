package com.divroll.webdash.server.service;

import com.divroll.webdash.shared.Subdomain;
import com.divroll.webdash.shared.Subdomains;

public interface SubdomainService {
    public Subdomain create(Subdomain subdomain);
    public Subdomains findByUser(Long userId);
    public boolean exists(String subdomain);
}
