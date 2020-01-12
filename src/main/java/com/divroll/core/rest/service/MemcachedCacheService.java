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

import net.spy.memcached.*;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
@Deprecated
public class MemcachedCacheService implements CacheService {

    final static Logger LOG
            = LoggerFactory.getLogger(MemcachedCacheService.class);

    public static final int MEMCACHED_EXPIRY_ONE_HOUR = 60 * 60;
    public static final int MEMCACHED_TIMEOUT = 60;

    private MemcachedClient mc;
    private List<String> address;

    @Override
    public String getString(String key) {
        if(address == null || address.isEmpty()) {
            throw new RuntimeException("Memcached address not configured");
        }
        try {
            if(mc == null) {
                mc = getMemcached();
            }
            Object value = mc.get(key);
            if(value != null) {
                LOG.info("=================================================================================");
                LOG.info("KEY   : " + key);
                LOG.info("VALUE : " + value);
                LOG.info("=================================================================================");
                return String.valueOf(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mc.shutdown();
            mc = null;
        } finally {
            //mc.shutdown();
        }
        return null;
    }

    @Override
    public void putString(String key, int expiration, String value) {
        if(address == null || address.isEmpty()) {
            throw new RuntimeException("Memcached address not configured");
        }
        try {
            if(mc == null) {
                mc = getMemcached();
            }
            mc.set(key, expiration, value).get();
        } catch (Exception e) {
            e.printStackTrace();
            mc.shutdown();
            mc = null;
        } finally {
            //mc.shutdown();
        }
    }

    @Override
    public void putString(String key, String value) {
        putString(key, MEMCACHED_EXPIRY_ONE_HOUR, value);
    }

    @Override
    public byte[] get(String key) {
        if(address == null || address.isEmpty()) {
            throw new RuntimeException("Memcached address not configured");
        }
        try {
            if(mc == null) {
                mc = getMemcached();
            }
            Object value = mc.get(key);
            if(value != null) {
                LOG.info("=================================================================================");
                LOG.info("KEY   : " + key);
                LOG.info("VALUE : " + value);
                LOG.info("=================================================================================");
                return (byte[]) value;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mc.shutdown();
            mc = null;
        } finally {
            //mc.shutdown();
        }
        return null;
    }

    @Override
    public void put(String key, int expiration, byte[] value) {
        if(address == null || address.isEmpty()) {
            throw new RuntimeException("Memcached address not configured");
        }
        LOG.info("Byte caching: " + key);
        ConnectionFactory factory = null;
        try {
            if(mc == null) {
                factory = new ConnectionFactoryBuilder()
                        .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
                        .setOpTimeout(MEMCACHED_TIMEOUT)
                        .build();
                mc = new MemcachedClient(factory, AddrUtil.getAddresses(address));
            }
            mc.set(key, expiration, value).get();
        } catch (Exception e) {
            e.printStackTrace();
            mc.shutdown();
            mc = null;
        } finally {
            //mc.shutdown();
        }
    }

    @Override
    public void put(String key, byte[] value) {
        put(key, MEMCACHED_EXPIRY_ONE_HOUR, value);
    }

    @Override
    public void delete(String key) {
        if(address == null || address.isEmpty()) {
            throw new RuntimeException("Memcached address not configured");
        }
        try {
            if(mc != null) {
                mc.delete(key);
            } else {
                mc = getMemcached();
                mc.delete(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mc.shutdown();
            mc = null;
        }
    }

    @Override
    public void setAddress(List<String> address) {
        this.address = address;
    }

    /**
     * Factory method to return a new instance of Memcached client.
     *
     * @return MemcachedClient
     * @throws IOException
     */
    private MemcachedClient getMemcached() throws IOException {
        ConnectionFactory factory = new ConnectionFactoryBuilder()
                .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
                .setFailureMode(FailureMode.Redistribute)
                .setOpTimeout(MEMCACHED_TIMEOUT)
                .build();
        return new MemcachedClient(factory, AddrUtil.getAddresses(address));
    }

}
