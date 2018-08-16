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
import org.restlet.ext.swagger.SwaggerSpecificationRestlet;
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
        corsFilter.setAllowedHeaders(Sets.newHashSet(Constants.HEADER_MASTER_KEY,
                Constants.HEADER_API_KEY,
                Constants.HEADER_APP_ID,
                Constants.HEADER_AUTH_TOKEN,
                Constants.HEADER_ACCEPT,
                Constants.HEADER_CONTENT_TYPE));
        corsFilter.setAllowedCredentials(true);

        router.attach(DOMINO_ROOT_URI + "applications", JeeApplicationServerResource.class); // TODO: Rename to directories
        router.attach(DOMINO_ROOT_URI + "entities/users", JeeUsersServerReource.class);
        router.attach(DOMINO_ROOT_URI + "entities/users/login", JeeUserServerResource.class);
        router.attach(DOMINO_ROOT_URI + "entities/users/{userId}", JeeUserServerResource.class);
        router.attach(DOMINO_ROOT_URI + "entities/roles", JeeRolesServerReource.class);
        router.attach(DOMINO_ROOT_URI + "entities/roles/{roleId}", JeeRoleServerResource.class);

        router.attach(DOMINO_ROOT_URI + "entities/roles/{roleId}/users/{userId}", JeeRoleServerResource.class);

        router.attach(DOMINO_ROOT_URI + "entities", JeeEntitiesServerResource.class);
        router.attach(DOMINO_ROOT_URI + "entities/{entityType}", JeeEntitiesServerResource.class);
        router.attach(DOMINO_ROOT_URI + "entities/{entityType}/{entityId}", JeeEntityServerResource.class);

        router.attach(DOMINO_ROOT_URI + "kv/{entityType}", JeeKeyValueServerResource.class);
        router.attach(DOMINO_ROOT_URI + "kv/{entityType}/{entityId}", JeeKeyValueServerResource.class);

        router.attachDefault(JeeRootServerResource.class);

        attachSwaggerSpecification2(router);

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

    /**
     * Adds the "/api-docs" path to the given router and attaches the
     * {@link Restlet} that computes the Swagger documentation in the format
     * defined by the swagger-spec project v1.2.
     *
     * @param router The router to update.
     */
    private void attachSwaggerSpecification1(Router router) {
        SwaggerSpecificationRestlet swaggerSpecificationRestlet = new SwaggerSpecificationRestlet(
                this);
        swaggerSpecificationRestlet.setBasePath("http://localhost:8080/");
        swaggerSpecificationRestlet.attach(router, "/docs");
    }

    /**
     * Adds the "/swagger.json" path to the given router and attaches the
     * {@link Restlet} that computes the Swagger documentation in the format
     * defined by the swagger-spec project v2.0.
     *
     * @param router The router to update.
     */
    private void attachSwaggerSpecification2(Router router) {
        Swagger2SpecificationRestlet restlet = new Swagger2SpecificationRestlet(
                this);
        restlet.setBasePath("http://localhost:8080/");
        restlet.attach(router, "/docs");
    }

}
