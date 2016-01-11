package com.divroll.webdash.server.repository.gae;

import com.divroll.webdash.client.shared.User;
import com.google.appengine.api.datastore.Key;
import com.hunchee.twist.types.Function;
import com.divroll.webdash.server.repository.UserRepository;

import java.util.logging.Logger;

import static com.hunchee.twist.ObjectStoreService.store;

public class GaeUserRepository implements UserRepository {

    private static final Logger LOG
            = Logger.getLogger(GaeUserRepository.class.getName());

    @Override
    public User save(User entity) {
        store().put(entity);
        return entity;
    }

    @Override
    public User findOne(Key primaryKey) {
        User user = store().get(User.class, primaryKey);
        return user;
    }

    @Override
    public Iterable<User> findAll() {
        return store().find(User.class).asIterable();
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
    public void delete(final Iterable<? extends User> entities) {
        store().transact(new Function<Void>() {
            @Override
            public Void execute() {
                for(User entity : entities){
                    store().delete(User.class, entity.getId());
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
        User stored = store().get(User.class, primaryKey);
        return (stored == null);
    }

    @Override
    public User findOne(Long userId) {
        return store().get(User.class, userId);
    }

    @Override
    public User findOne(String username) {
        return (User) store().find(User.class).equal("username", username).first();
    }
}
