/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.service.jee;

import com.divroll.backend.service.CacheService;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Deprecated
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

    @Override
    public boolean isExists(String key) {
        if(cache == null) {
            cache = buildCache();
        }
        return cache.containsKey(key);
    }

    private Cache<String, String> buildCache() {
        if(cacheManager == null) {
            cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                    .withCache("preConfigured",
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                                    ResourcePoolsBuilder.heap(100))
                                     .withExpiry(Expirations.timeToIdleExpiration(Duration.of(24, TimeUnit.HOURS)))
                                    .build())
                    .build(true);
        }
        cache = cacheManager.createCache("defaultCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                        ResourcePoolsBuilder.heap(100))
                        .withExpiry(Expirations.timeToIdleExpiration(Duration.of(24, TimeUnit.HOURS)))
                        .build());
        return cache;
    }
}
