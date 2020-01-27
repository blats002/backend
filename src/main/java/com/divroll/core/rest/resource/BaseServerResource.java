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
package com.divroll.core.rest.resource;

import com.divroll.core.rest.guice.SelfInjectingServerResource;
import com.divroll.core.rest.service.CacheService;
import com.divroll.core.rest.service.PrerenderService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class BaseServerResource extends SelfInjectingServerResource {

    final static Logger LOG
            = LoggerFactory.getLogger(BaseServerResource.class);

    protected final static String APPLICATION_BASE_URI = "/applications/";

    @Inject
    @Named("divroll.serverUrl")
    protected String serverUrl;

    @Inject
    @Named("divroll.masterToken")
    protected String masterToken;

    @Inject
    @Named("app.domain")
    protected String appDomain;

    @Inject
    @Named("app.domain.local")
    protected String appDomainLocal;

    @Inject
    @Named("prerender.timeout")
    protected String prerenderTimeout;

    @Inject
    @Named("prerender.windowTimeout")
    protected String prerenderWindowTimeout;

    @Inject
    protected CacheService cacheService;

    @Inject
    protected PrerenderService prerenderService;

    @Override
    protected void doInit() {
        super.doInit();

        String prerenderTimeoutEnv = System.getenv("PRERENDER_TIMEOUT");
        String prerenderWindowTimeoutEnv = System.getenv("PRERENDER_WINDOW_TIMEOUT");
        String serverUrlEnv = System.getenv("DIVROLL_SERVER_URL");
        String masterTokenEnv = System.getenv("DIVROLL_MASTER_TOKEN");

        if(prerenderTimeoutEnv != null && !prerenderTimeoutEnv.isEmpty()) {
            prerenderTimeout = prerenderTimeoutEnv;
        }

        if(prerenderWindowTimeoutEnv != null && !prerenderWindowTimeoutEnv.isEmpty()) {
            prerenderWindowTimeout = prerenderWindowTimeoutEnv;
        }

        if(serverUrlEnv != null && !serverUrlEnv.isEmpty()) {
            serverUrl = serverUrlEnv;
        }

        if(masterTokenEnv != null && !masterTokenEnv.isEmpty()) {
            masterToken = masterTokenEnv;
        }

    }

    protected boolean isDevmode() {
        String environment = System.getenv("DIVROLL_ENVIRONMENT");
        //LOG.info("Environment:  " + environment);
        if(environment == null) {
            return false;
        }
        return environment.equals("development");
    }

}
