package com.divroll.webdash.server.repository.gae;

import com.divroll.webdash.shared.Blog;
import com.divroll.webdash.server.repository.BlogRepository;
import com.google.appengine.api.datastore.Key;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeBlogRepository implements BlogRepository {

    private static final Logger LOG
            = Logger.getLogger(GaeBlogRepository.class.getName());

    @Override
    public Blog save(Blog entity) {
        return null;
    }

    @Override
    public Blog findOne(Key primaryKey) {
        return null;
    }

    @Override
    public Iterable<Blog> findAll() {
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
    public void delete(Iterable<? extends Blog> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public boolean exists(Key primaryKey) {
        return false;
    }
}
