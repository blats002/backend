/*
*
* Copyright (c) 2016 Kerby Martino and Divroll. All Rights Reserved.
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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import org.restlet.ext.servlet.ServerServlet;

import java.util.Map;

/**
 * @author <a href="mailto:kerby@hunchee.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class GuiceServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(ServerServlet.class).in(Scopes.SINGLETON);
//        serve("/*").with(ServerServlet.class, map("org.restlet.application", "DivrollApplication"));
    }

    private static Map<String,String> map(String... params) {
        Preconditions.checkArgument(params.length % 2 == 0, "You have to have a n even number of map params");
        Map<String,String> map = Maps.newHashMap();
        for (int i = 0; i < params.length; i+=2) {
            map.put(params[i], params[i+1]);
        }
        return map;
    }
}
