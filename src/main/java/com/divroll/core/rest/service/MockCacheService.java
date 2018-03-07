package com.divroll.core.rest.service;

import java.util.List;

public class MockCacheService implements CacheService {
    @Override
    public String getString(String key) {
        return null;
    }

    @Override
    public void putString(String key, int expiration, String value) {

    }

    @Override
    public void putString(String key, String value) {

    }

    @Override
    public byte[] get(String key) {
        return null;
    }

    @Override
    public void put(String key, int expiration, byte[] value) {

    }

    @Override
    public void put(String key, byte[] value) {

    }

    @Override
    public void delete(String key) {

    }

    @Override
    public void setAddress(List<String> address) {

    }
}
