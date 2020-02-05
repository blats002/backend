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

import com.divroll.core.rest.service.CacheService;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class EhcacheCacheService implements CacheService {

    CacheManager cacheManager;
    Cache<String, byte[]> cache;

    @Override
    public String getString(String key) {
        if(cache == null) {
            cache = buildCache();
        }
        byte[] bytes = cache.get(key);
        if(bytes != null) {
            return new String(bytes);
        }
        return null;
    }

    @Override
    public void putString(String key, int expiration, String value) {
        if(cache == null) {
            cache = buildCache();
        }
        try {
            cache.put(key, value.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void putString(String key, String value) {
        if(cache == null) {
            cache = buildCache();
        }
        try {
            cache.put(key, value.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] get(String key) {
        if(cache == null) {
            cache = buildCache();
        }
        //return cache.get(key) != null ? cache.get(key).getBytes(StandardCharsets.UTF_8) : null;
        return cache.get(key);
    }

    @Override
    public void put(String key, int expiration, byte[] value) {
        if(cache == null) {
            cache = buildCache();
        }
        cache.put(key, value);
    }

    @Override
    public void put(String key, byte[] value) {
        if(cache == null) {
            cache = buildCache();
        }
        cache.put(key, value);
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

    private Cache<String, byte[]> buildCache() {
        if(cacheManager == null) {
            cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                    .withCache("preConfigured",
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
                                    ResourcePoolsBuilder.heap(100).offheap(100, MemoryUnit.MB))
                                    .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(60)))
                                    .build())
                    .build(true);
        }
        cache = cacheManager.createCache("defaultCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
                        ResourcePoolsBuilder.heap(100).offheap(100, MemoryUnit.MB))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(60)))
                        .build());
        return cache;
    }
}
