package com.divroll.webdash.server.repository.gae;

import com.divroll.webdash.client.shared.Subdomain;
import com.divroll.webdash.server.repository.SubdomainRepository;
import com.google.appengine.api.datastore.Key;

import java.util.logging.Logger;

public class GaeSubdomainRepository implements SubdomainRepository {

    private static final Logger LOG
            = Logger.getLogger(GaeSubdomainRepository.class.getName());

    @Override
    public Subdomain save(Subdomain entity) {
        return null;
    }

    @Override
    public Subdomain findOne(Key primaryKey) {
        return null;
    }

    @Override
    public Iterable<Subdomain> findAll() {
        return null;
    }

    @Override
    public Long count() {
        return null;
    }

    @Override
    public void delete(Key primaryKey) {

    }

    @Override
    public void delete(Iterable<? extends Subdomain> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public boolean exists(Key primaryKey) {
        return false;
    }
}
