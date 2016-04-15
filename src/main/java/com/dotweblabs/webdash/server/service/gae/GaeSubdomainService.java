package com.divroll.webdash.server.service.gae;

import com.divroll.webdash.client.shared.Subdomain;
import com.divroll.webdash.client.shared.Subdomains;
import com.divroll.webdash.server.repository.SubdomainRepository;
import com.divroll.webdash.server.service.SubdomainService;
import com.google.inject.Inject;

import java.util.logging.Logger;

public class GaeSubdomainService implements SubdomainService {

    private static final Logger LOG
            = Logger.getLogger(GaeSubdomainService.class.getName());

    @Inject
    SubdomainRepository subdomainRepository;

    @Override
    public Subdomain create(Subdomain subdomain) {
        // TODO: Validation
        return subdomainRepository.save(subdomain);
    }

    @Override
    public Subdomains findByUser(Long userId) {
        return subdomainRepository.findByUser(userId);
    }

    @Override
    public boolean exists(String subdomain) {
        return false;
    }
}
