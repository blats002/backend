package com.divroll.webdash.server.repository;

import com.google.appengine.api.datastore.Key;

public interface CrudRepository<T> {
    public T save(T entity);
    public T findOne(Key primaryKey);
    public Iterable<T> findAll();
    public Long count();
    public void delete(Key primaryKey);
    public void delete(Iterable<? extends T> entities);
    public void deleteAll();
    public boolean exists(Key primaryKey);
}