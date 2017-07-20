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
package com.divroll.core.rest.resource;

import com.divroll.core.rest.guice.SelfInjectingServerResource;
import com.divroll.core.rest.service.CacheService;
import com.divroll.core.rest.util.StringUtil;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class BaseServerResource extends SelfInjectingServerResource {

    final static Logger LOG
            = LoggerFactory.getLogger(BaseServerResource.class);

    @Inject
    @Named("app.domain")
    protected String appDomain;

    @Inject
    @Named("app.domain.local")
    protected String appDomainLocal;

    @Inject
    @Named("parse.url.dev")
    protected String parseUrlDev;

    @Inject
    @Named("parse.url")
    protected String parseUrl;

    @Inject
    @Named("parse.appid")
    protected String parseAppId;

    @Inject
    @Named("parse.restapikey")
    protected String parseRestAPIkey;

    @Inject
    @Named("parse.publicurl")
    protected String parsePublicUrl;

    @Inject
    @Named("prerender.url.dev")
    protected String prerenderUrlDev;

    @Inject
    @Named("prerender.url")
    protected String prerenderUrl;

    @Inject
    @Named("memcached.address")
    protected String memcachedAddress;

    @Inject
    @Named("memcached.address.dev")
    protected String memcachedAddressDev;

    @Inject
    protected CacheService cacheService;

    @Override
    protected void doInit() {
        super.doInit();
        cacheService.setAddress(getMemcachedAddress());
    }

    public List<String> getMemcachedAddress() {
        List<String> address = new LinkedList<String>();
        boolean isDevmode = isDevmode();
        if(isDevmode) {
            address.add(memcachedAddressDev);
        } else {
            if(memcachedAddress.contains(",")) {
                address = StringUtil.asList(memcachedAddress);
            } else {
                address.add(memcachedAddress);
            }
        }
        return address;
    }

    protected String getPrerenderUrl() {
        String prerenderUrlString = isDevmode() ? prerenderUrlDev : prerenderUrl;
        return prerenderUrlString;
    }

    protected String getParseServerUrl() {
        String serverUrl = isDevmode() ? parseUrlDev : parseUrl;
        return serverUrl;
    }

    protected String getParseAppId() {
        return parseAppId;
    }

    protected String getParseRestAPIkey() {
        return parseRestAPIkey;
    }

    protected boolean isDevmode() {
        String environment = System.getenv("DIVROLL_ENVIRONMENT");
        //System.out.println("Environment:  " + environment);
        if(environment == null) {
            return false;
        }
        return environment.equals("development");
    }

}
