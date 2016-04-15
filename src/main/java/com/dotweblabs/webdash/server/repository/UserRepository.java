package com.divroll.webdash.server.repository;

import com.divroll.webdash.client.shared.User;

public interface UserRepository extends CrudRepository<User> {
    public User findOne(Long userId);
    public User findOne(String username);
}
