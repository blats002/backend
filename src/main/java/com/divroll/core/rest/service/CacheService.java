/*
*
* Copyright (c) 2017 Kerby Martino and Divroll. All Rights Reserved.
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
package com.divroll.core.rest.service;

import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public interface CacheService {
    String getString(String key);
    void putString(String key, int expiration, String value);
    void putString(String key, String value);
    byte[] get(String key);
    void put(String key, int expiration, byte[] value);
    void put(String key, byte[] value);
    void delete(String key);
    @Deprecated
    void setAddress(List<String> address);
}
