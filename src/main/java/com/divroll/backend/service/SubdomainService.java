package com.divroll.backend.service;

public interface SubdomainService {
    boolean isValidSubdomain(String subdomain);
    String retrieveDomain(String subdomain);
    String retrieveAppId(String subdomain);
}
