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
package com.divroll.webdash.server.service;

import com.divroll.webdash.client.shared.User;
import com.divroll.webdash.client.shared.Users;
import com.divroll.webdash.server.service.exception.ValidationException;

public interface UserService {
    public User save(User user) throws ValidationException;
    public User saveNew(User user) throws ValidationException;
    public User read(String username) throws ValidationException;
    public User read(Long userId) throws ValidationException;
    public User update(User user) throws ValidationException;
    public User updateUserEmail(String username, String newEmail) throws ValidationException;
    public Users list();
}
