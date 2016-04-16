package com.divroll.webdash.server.repository.gae;

import com.divroll.webdash.shared.Subdomain;
import com.divroll.webdash.shared.Subdomains;
import com.divroll.webdash.server.repository.SubdomainRepository;
import com.google.appengine.api.datastore.Key;
import com.hunchee.twist.types.Function;
import com.hunchee.twist.types.ListResult;

import java.util.logging.Logger;

import static com.hunchee.twist.ObjectStoreService.store;

public class GaeSubdomainRepository implements SubdomainRepository {

    private static final Logger LOG
            = Logger.getLogger(GaeSubdomainRepository.class.getName());

    @Override
    public Subdomain save(Subdomain entity) {
        store().put(entity);
        return entity;
    }

    @Override
    public Subdomain findOne(Key primaryKey) {
        return store().get(Subdomain.class, primaryKey);
    }

    @Override
    public Iterable<Subdomain> findAll() {
        return store().find(Subdomain.class).asIterable();
    }

    @Override
    public Subdomains findByUser(Long id) {
        ListResult<Subdomain> result = store().find(Subdomain.class)
                .equal("userId", id).asList();
        Subdomains userDomains = new Subdomains();
        userDomains.setCursor(result.getWebsafeCursor());
        userDomains.setList(result.getList());
        return userDomains;
    }

    @Override
    public Long count() {
        return null;
    }

    @Override
    public void delete(Key primaryKey) {
        store().delete(primaryKey);
    }

    @Override
    public void delete(final Iterable<? extends Subdomain> entities) {
        store().transact(new Function<Void>() {
            @Override
            public Void execute() {
                for(Subdomain subdomain : entities){
                    Long id = subdomain.getId();
                    store().delete(Subdomain.class, id);
                }
                return null;
            }
        });
    }

    @Override
    public void deleteAll() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public boolean exists(Key primaryKey) {
        Subdomain result = store().get(Subdomain.class, primaryKey);
        return (result != null);
    }


}
