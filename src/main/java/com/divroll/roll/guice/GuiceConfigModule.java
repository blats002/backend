/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
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
package com.divroll.roll.guice;

import com.divroll.roll.Constants;
import com.divroll.roll.repository.EntityRepository;
import com.divroll.roll.repository.RoleRepository;
import com.divroll.roll.repository.UserRepository;
import com.divroll.roll.repository.jee.JeeEntityRepository;
import com.divroll.roll.repository.jee.JeeRoleRepository;
import com.divroll.roll.repository.jee.JeeUserRepository;
import com.divroll.roll.service.ApplicationService;
import com.divroll.roll.service.KeyValueService;
import com.divroll.roll.service.WebTokenService;
import com.divroll.roll.service.jee.JeeApplicationService;
import com.divroll.roll.service.jee.JeeKeyValueService;
import com.divroll.roll.service.jee.JeeWebTokenService;
import com.divroll.roll.xodus.*;
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
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class GuiceConfigModule extends AbstractModule {

    private static final Logger LOG = Logger.getLogger(GuiceConfigModule.class.getName());
    private Context context;

    public GuiceConfigModule() {
    }

    public GuiceConfigModule(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void configure() {
        Logger.getLogger("com.google.inject.internal.util").setLevel(Level.WARNING);
        bind(String.class).annotatedWith(Names.named("app")).toInstance(Constants.SERVER_NAME);

        bind(XodusStore.class).to(XodusStoreImpl.class).in(Scopes.SINGLETON);
        bind(XodusEnvStore.class).to(XodusEnvStoreImpl.class).in(Scopes.SINGLETON);
        bind(XodusManager.class).to(XodusManagerImpl.class).in(Scopes.SINGLETON);

        bind(UserRepository.class).to(JeeUserRepository.class).in(Scopes.SINGLETON);
        bind(RoleRepository.class).to(JeeRoleRepository.class).in(Scopes.SINGLETON);
        bind(EntityRepository.class).to(JeeEntityRepository.class).in(Scopes.SINGLETON);

        bind(WebTokenService.class).to(JeeWebTokenService.class).in(Scopes.SINGLETON);
        bind(ApplicationService.class).to(JeeApplicationService.class).in(Scopes.SINGLETON);
        bind(KeyValueService.class).to(JeeKeyValueService.class).in(Scopes.SINGLETON);

        Names.bindProperties(binder(), readProperties());
    }

    protected Properties readProperties() {
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