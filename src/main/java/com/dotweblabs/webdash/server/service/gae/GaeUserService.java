/*
*
* Copyright (c) 2015 Kerby Martino and Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   https://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/
package com.divroll.webdash.server.service.gae;

import com.google.inject.Inject;
import com.divroll.webdash.shared.User;
import com.divroll.webdash.server.repository.UserRepository;
import com.divroll.webdash.server.service.UserService;
import com.divroll.webdash.server.service.exception.ValidationException;
import java.util.Date;
import java.util.logging.Logger;

public class GaeUserService implements UserService {

    private static final Logger LOG
            = Logger.getLogger(GaeUserService.class.getName());

    @Inject
    UserRepository repository;

    @Override
    public User save(User user) throws ValidationException {
        user.setCreated(new Date());
        user.setModified(new Date());
        validate(user);
        repository.save(user);
        return user;
    }

    @Override
    public User saveNew(User user) throws ValidationException {
        User saved = read(user.getUsername());
        if(saved == null){
            return save(user);
        }
        return null;
    }

    @Override
    public User read(String username) throws ValidationException {
      User user = repository.findOne(username);
      return user;
    }

    @Override
    public User read(Long userId) throws ValidationException {
        return repository.findOne(userId);
    }

    @Override
    public User update(User user) throws ValidationException {
        User saved = repository.findOne(user.getUsername());
        if(saved != null){
            user.setCreated(saved.getCreated());
            user.setModified(saved.getModified());
            validate(user);
            repository.save(user);
        }
        return user;
    }

    @Override
    public User updateUserEmail(String username, String newEmail) throws ValidationException {
        User user = repository.findOne(username);
        if(user != null){
            user.setModified(user.getModified());
            user.setEmail(newEmail);
            validate(user);
            repository.save(user);
        }
        return user;
    }

    private void validate(User user) throws ValidationException {
        // TODO - Add validation code
    }

}
