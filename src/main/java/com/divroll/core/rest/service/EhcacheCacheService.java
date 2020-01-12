package com.divroll.core.rest.service;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class EhcacheCacheService implements CacheService {

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
