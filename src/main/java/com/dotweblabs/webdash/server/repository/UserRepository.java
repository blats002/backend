package com.divroll.webdash.server.repository;

import com.divroll.webdash.client.shared.User;

public interface UserRepository extends CrudRepository<User> {
    public User findOne(String userId);
}
