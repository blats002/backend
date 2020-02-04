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
package com.divroll.core.rest.guice;

import com.divroll.core.rest.service.*;
import com.divroll.core.rest.service.impl.ChromeDriverPrerenderService;
import com.divroll.core.rest.service.impl.EhcacheCacheService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.restlet.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        bind(CacheService.class).to(EhcacheCacheService.class).in(Scopes.SINGLETON);
        bind(PrerenderService.class).to(ChromeDriverPrerenderService.class).in(Scopes.SINGLETON);
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
