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

import com.divroll.core.rest.util.StringUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;

import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
@Deprecated
public class RedisCacheService implements CacheService {

    private RedisClient redisClient;
    private StatefulRedisConnection<byte[], byte[]> connection;
    private String connectionUrl;

    @Override
    public String getString(String key) {
        if(connectionUrl == null || connectionUrl.isEmpty()) {
            throw new RuntimeException("Redis URL not configured");
        }
        try {
            if(connection == null || !connection.isOpen()) {
                redisClient = RedisClient.create(connectionUrl);
                connection = redisClient.connect(new ByteArrayCodec());
                System.out.println("Connected to Redis");
            }
            RedisCommands<byte[], byte[]> syncCommands = connection.sync();
            byte[] keyBytes = StringUtil.toByteArray(key);
            byte[] result = syncCommands.get(keyBytes);
            if(result == null) {
                return null;
            }
            return StringUtil.fromByteArray(result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //redisClient.shutdown();
            //connection.close();
        }
        return null;
    }

    @Override
    public void putString(String key, int expiration, String value) {
        if(connectionUrl == null || connectionUrl.isEmpty()) {
            throw new RuntimeException("Redis URL not configured");
        }
        try {
            if(connection == null || !connection.isOpen()) {
                redisClient = RedisClient.create(connectionUrl);
                connection = redisClient.connect(new ByteArrayCodec());
                System.out.println("Connected to Redis");
            }
            RedisCommands<byte[], byte[]> syncCommands = connection.sync();
            byte[] keyBytes = StringUtil.toByteArray(key);
            byte[] valueBytes = StringUtil.toByteArray(value);
            syncCommands.set(keyBytes, valueBytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //redisClient.shutdown();
            //connection.close();
        }
    }

    @Override
    public void putString(String key, String value) {
        putString(key, 0, value);
    }

    @Override
    public byte[] get(String key) {
        if(connectionUrl == null || connectionUrl.isEmpty()) {
            throw new RuntimeException("Redis URL not configured");
        }
        try {
            if(connection == null || !connection.isOpen()) {
                redisClient = RedisClient.create(connectionUrl);
                connection = redisClient.connect(new ByteArrayCodec());
                System.out.println("Connected to Redis");
            }
            RedisCommands<byte[], byte[]> syncCommands = connection.sync();
            byte[] keyBytes = StringUtil.toByteArray(key);
            return syncCommands.get(keyBytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //redisClient.shutdown();
            //connection.close();
        }
        return null;
    }

    @Override
    public void put(String key, int expiration, byte[] value) {
        put(key, 0, value);
    }

    @Override
    public void put(String key, byte[] value) {
        if(connectionUrl == null || connectionUrl.isEmpty()) {
            throw new RuntimeException("Redis URL not configured");
        }
        try {
            if(connection == null || !connection.isOpen()) {
                redisClient = RedisClient.create(connectionUrl);
                connection = redisClient.connect(new ByteArrayCodec());
                System.out.println("Connected to Redis");
            }
            RedisCommands<byte[], byte[]> syncCommands = connection.sync();
            byte[] keyBytes = StringUtil.toByteArray(key);;
            syncCommands.set(keyBytes, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //redisClient.shutdown();
            //connection.close();
        }
    }

    @Override
    public void delete(String key) {
        if(connectionUrl == null || connectionUrl.isEmpty()) {
            throw new RuntimeException("Redis URL not configured");
        }
        try {
            if(connection == null || !connection.isOpen()) {
                redisClient = RedisClient.create(connectionUrl);
                connection = redisClient.connect(new ByteArrayCodec());
                System.out.println("Connected to Redis");
            }
            RedisCommands<byte[], byte[]> syncCommands = connection.sync();
            byte[] keyBytes = StringUtil.toByteArray(key);;
            syncCommands.del(keyBytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //redisClient.shutdown();
            //connection.close();
        }
    }

    @Override
    public void setAddress(List<String> address) {
        if(address != null && !address.isEmpty()) {
            connectionUrl = address.iterator().next();
        }
    }
}
