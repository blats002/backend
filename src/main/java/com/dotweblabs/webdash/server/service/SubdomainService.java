package com.divroll.webdash.server.service;

import com.divroll.webdash.client.shared.Subdomain;

public interface SubdomainService {
    public Subdomain create(Subdomain subdomain);
    public boolean exists(String subdomain);
}
