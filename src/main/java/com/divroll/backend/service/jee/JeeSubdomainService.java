package com.divroll.backend.service.jee;

import com.divroll.backend.service.SubdomainService;

public class JeeSubdomainService implements SubdomainService {
    @Override
    public boolean isValidSubdomain(String subdomain) {
        return false;
    }

    @Override
    public String retrieveDomain(String subdomain) {
        return null;
    }

    @Override
    public String retrieveAppId(String subdomain) {
        return null;
    }
}
