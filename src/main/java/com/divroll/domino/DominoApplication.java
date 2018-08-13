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
package com.divroll.domino;

import com.divroll.domino.guice.GuiceConfigModule;
import com.divroll.domino.guice.SelfInjectingServerResourceModule;
import com.divroll.domino.resource.jee.*;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.engine.Engine;
import org.restlet.engine.application.CorsFilter;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.swagger.Swagger2SpecificationRestlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class DominoApplication extends Application {

    private static final Logger LOG
            = Logger.getLogger(DominoApplication.class.getName());
    private static final String ROOT_URI = "/";
    private static final String DOMINO_ROOT_URI = "/domino/";

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public Restlet createInboundRoot() {
        Guice.createInjector(new GuiceConfigModule(this.getContext()),
                new SelfInjectingServerResourceModule());
        LOG.info("Starting application");

        configureConverters();

        Router router = new Router(getContext());

        CorsFilter corsFilter = new CorsFilter(getContext());
        corsFilter.setAllowedOrigins(new HashSet(Arrays.asList("*")));
        corsFilter.setAllowedHeaders(Sets.newHashSet("X-Domino-Master-Key",
                "X-Domino-Api-Key",
                "X-Domino-App-Id",
                "X-Domino-Auth-Token",
                "Accept",
                "Content-Type"));
        corsFilter.setAllowedCredentials(true);


        Directory directory = new Directory(getContext(), "war:///doc");
        directory.setIndexName("index.html");
        router.attach(ROOT_URI + "ping", JeePingServerResource.class);
        router.attach(ROOT_URI + "ping/", JeePingServerResource.class);
        router.attach(DOMINO_ROOT_URI + "applications", JeeApplicationServerResource.class); // TODO: Rename to directories
        router.attach(DOMINO_ROOT_URI + "entities/users", JeeUserServerResource.class);
        router.attach(DOMINO_ROOT_URI + "entities/{kind}", JeeKeyValueServerResource.class);
        router.attach(DOMINO_ROOT_URI + "entities/{kind}/{entityId}", JeeKeyValueServerResource.class);
        router.attach("/directory/", directory);
        // Configuring Swagger 2 support
        Swagger2SpecificationRestlet swagger2SpecificationRestlet
                = new Swagger2SpecificationRestlet(this);
        swagger2SpecificationRestlet.setBasePath("/api-docs");
        swagger2SpecificationRestlet.attach(router);

        router.attachDefault(JeeRootServerResource.class);
        corsFilter.setNext(router);

        return corsFilter;

    }

    private Map<String, String> appProperties() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        InputStream is = getContext().getClass().getResourceAsStream("/app.properties");
        Properties props = new Properties();
        try {
            props.load(is);
            map = new LinkedHashMap<String, String>((Map) props);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void configureConverters() {
        List<ConverterHelper> converters = Engine.getInstance()
                .getRegisteredConverters();
        JacksonConverter jacksonConverter = null;
        for (ConverterHelper converterHelper : converters) {
            System.err.println(converterHelper.getClass());
            if (converterHelper instanceof JacksonConverter) {
                jacksonConverter = (JacksonConverter) converterHelper;
                break;
            }
        }
        if (jacksonConverter != null) {
            Engine.getInstance()
                    .getRegisteredConverters().remove(jacksonConverter);
        }
    }

}
