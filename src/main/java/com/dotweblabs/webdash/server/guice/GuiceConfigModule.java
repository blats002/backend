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
package com.divroll.webdash.server.guice;

import com.divroll.webdash.server.repository.BlogRepository;
import com.divroll.webdash.server.repository.FileRepository;
import com.divroll.webdash.server.repository.ValueRepository;
import com.divroll.webdash.server.repository.gae.GaeBlogRepository;
import com.divroll.webdash.server.repository.gae.GaeFileRepository;
import com.divroll.webdash.server.repository.gae.GaeValueRepository;
import com.divroll.webdash.server.service.*;
import com.divroll.webdash.server.service.gae.*;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.divroll.webdash.server.repository.UserRepository;
import com.divroll.webdash.server.repository.gae.GaeUserRepository;
import org.restlet.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:kerby@hunchee.com">Kerby Martino</a>
 * @author <a href="mailto:kerbymart@gmail.com">Kerby Martino</a>
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
        bind(String.class).annotatedWith(Names.named("app")).toInstance("Webdash");
        bind(BlogRepository.class).to(GaeBlogRepository.class).in(Scopes.SINGLETON);
        bind(FileRepository.class).to(GaeFileRepository.class).in(Scopes.SINGLETON);
        bind(UserRepository.class).to(GaeUserRepository.class).in(Scopes.SINGLETON);
        bind(ValueRepository.class).to(GaeValueRepository.class).in(Scopes.SINGLETON);
        bind(BlogService.class).to(GaeBlogService.class).in(Scopes.SINGLETON);
        bind(FileService.class).to(GaeFileService.class).in(Scopes.SINGLETON);
        bind(UserService.class).to(GaeUserService.class).in(Scopes.SINGLETON);
        bind(ValueService.class).to(GaeValueService.class).in(Scopes.SINGLETON);
        bind(WebTokenService.class).to(GaeWebTokenService.class).in(Scopes.SINGLETON);
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
