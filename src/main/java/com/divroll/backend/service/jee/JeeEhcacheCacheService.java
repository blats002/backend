/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.backend.service.jee;

import com.divroll.backend.service.CacheService;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class JeeEhcacheCacheService implements CacheService {

    CacheManager cacheManager;
    Cache<String, String> cache;

    @Override
    public String getString(String key) {
        if(cache == null) {
            cache = buildCache();
        }
        return cache.get(key);
    }

    @Override
    public void putString(String key, int expiration, String value) {
        if(cache == null) {
            cache = buildCache();
        }
        cache.put(key, value);
    }

    @Override
    public void putString(String key, String value) {
        if(cache == null) {
            cache = buildCache();
        }
        cache.put(key, value);
    }

    @Override
    public byte[] get(String key) {
        if(cache == null) {
            cache = buildCache();
        }
        return cache.get(key) != null ? cache.get(key).getBytes(StandardCharsets.UTF_8) : null;
    }

    @Override
    public void put(String key, int expiration, byte[] value) {
        if(cache == null) {
            cache = buildCache();
        }
        cache.put(key, new String(value, StandardCharsets.UTF_8));
    }

    @Override
    public void put(String key, byte[] value) {
        if(cache == null) {
            cache = buildCache();
        }
        cache.put(key, new String(value, StandardCharsets.UTF_8));
    }

    @Override
    public void delete(String key) {
        if(cache == null) {
            cache = buildCache();
        }
        cache.remove(key);
    }

    @Override
    public void setAddress(List<String> address) {

    }

    private Cache<String, String> buildCache() {
        if(cacheManager == null) {
            cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                    .withCache("preConfigured",
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                    ResourcePoolsBuilder.heap(100))
                                    .build())
                    .build(true);
        }
        cache = cacheManager.createCache("defaultCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                        ResourcePoolsBuilder.heap(100)).build());
        return cache;
    }
}
