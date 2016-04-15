package com.divroll.webdash.server.repository.gae;

import com.divroll.webdash.shared.Value;
import com.divroll.webdash.server.repository.ValueRepository;
import com.google.appengine.api.datastore.Key;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeValueRepository implements ValueRepository {

    private static final Logger LOG
            = Logger.getLogger(GaeValueRepository.class.getName());

    @Override
    public Value save(Value entity) {
        return null;
    }

    @Override
    public Value findOne(Key primaryKey) {
        return null;
    }

    @Override
    public Iterable<Value> findAll() {
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
    public void delete(Iterable<? extends Value> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public boolean exists(Key primaryKey) {
        return false;
    }
}
