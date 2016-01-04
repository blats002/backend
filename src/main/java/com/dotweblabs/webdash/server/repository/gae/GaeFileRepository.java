package com.divroll.webdash.server.repository.gae;

import com.divroll.webdash.client.shared.File;
import com.divroll.webdash.server.repository.FileRepository;
import com.google.appengine.api.datastore.Key;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeFileRepository implements FileRepository {

    private static final Logger LOG
            = Logger.getLogger(GaeFileRepository.class.getName());

    @Override
    public File save(File entity) {
        return null;
    }

    @Override
    public File findOne(Key primaryKey) {
        return null;
    }

    @Override
    public Iterable<File> findAll() {
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
    public void delete(Iterable<? extends File> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public boolean exists(Key primaryKey) {
        return false;
    }
}
