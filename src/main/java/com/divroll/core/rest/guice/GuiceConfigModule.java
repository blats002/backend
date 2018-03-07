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
package com.divroll.core.rest.guice;

import com.divroll.core.rest.service.CacheService;
import com.divroll.core.rest.service.MemcachedCacheService;
import com.divroll.core.rest.service.MockCacheService;
import com.divroll.core.rest.service.RedisCacheService;
import com.divroll.core.rest.util.StringUtil;
import com.google.common.io.ByteStreams;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.restlet.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class GuiceConfigModule extends AbstractModule {

    private static final Logger LOG = Logger.getLogger(GuiceConfigModule.class.getName());
    private Context context;

    public GuiceConfigModule(){}

    public GuiceConfigModule(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void configure() {
        // Suppress Guice warning when on GAE
        // see https://code.google.com/p/google-guice/issues/detail?id=488
        Logger.getLogger("com.google.inject.internal.util").setLevel(Level.WARNING);
        bind(CacheService.class).to(RedisCacheService.class).in(Scopes.SINGLETON);
//        bind(CacheService.class).to(MockCacheService.class).in(Scopes.SINGLETON);
        Names.bindProperties(binder(), readProperties());

    }

    protected Properties readProperties(){
        InputStream is = this.getClass().getResourceAsStream("/app.properties");
        Properties props = new Properties();
        try {
            props.load(is);
            return props;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
