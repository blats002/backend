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
package com.divroll.backend;

import com.divroll.backend.guice.GuiceConfigModule;
import com.divroll.backend.guice.SelfInjectingServerResourceModule;
import com.divroll.backend.resource.jee.*;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.apispark.internal.firewall.FirewallFilter;
import org.restlet.ext.apispark.internal.firewall.handler.RateLimitationHandler;
import org.restlet.ext.apispark.internal.firewall.handler.policy.UniqueLimitPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallRule;
import org.restlet.ext.apispark.internal.firewall.rule.PeriodicFirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.policy.IpAddressCountingPolicy;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.swagger.Swagger2SpecificationRestlet;
import org.restlet.ext.swagger.SwaggerSpecificationRestlet;
import org.restlet.routing.Router;
import org.restlet.engine.application.CorsFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class DivrollBackendApplication extends Application {

  private static final Logger LOG = LoggerFactory.getLogger(DivrollBackendApplication.class);

  private static final String ROOT_URI = "/";
  private static final String DIVROLL_ROOT_URI = "/divroll/";
  private static final int DEFAULT_RATE_LIMIT = 1000;

  /** Creates a root Restlet that will receive all incoming calls. */
  @Override
  public Restlet createInboundRoot() {

    LOG.info("Starting application");

    int rateLimit = DEFAULT_RATE_LIMIT;
    String rateLimitEnv = System.getenv("RATE_LIMIT");
    if(rateLimitEnv != null && !rateLimitEnv.isEmpty()) {
      rateLimit = Integer.valueOf(rateLimitEnv);
    }

    Guice.createInjector(
        new GuiceConfigModule(this.getContext()), new SelfInjectingServerResourceModule());

    configureConverters();
    configureJobScheduler();

    Router router = new Router(getContext());
    router.attachDefault(JeeSiteServerResource.class);

//    attachSwaggerSpecification2(router);
    attachSwaggerSpecification2(router);

    router.attach(DIVROLL_ROOT_URI + "version", JeeVersionServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "superusers", JeeSuperusersServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "superusers/login", JeeSuperuserServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "superusers/activate", JeeSuperuserActivateServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "superusers/resetPassword", JeeSuperuserPasswordResetServerResource.class);

    router.attach(DIVROLL_ROOT_URI + "sites", JeeSiteServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "sites/ssls", JeeSiteServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "sites/{siteId}", JeeSiteManagerServerResource.class);

    router.attach(DIVROLL_ROOT_URI + "applications/{appName}", JeeApplicationServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "applications/{appName}/domains/{domainName}", JeeDomainServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "domains", JeeDomainServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "applications", JeeApplicationsServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities", JeeEntityTypesServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/types/{entityType}", JeeEntityTypeServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/users", JeeUsersServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/users/login", JeeUserServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/users/resetPassword", JeePasswordResetServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/users/{userId}", JeeUserServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/roles", JeeRolesServerReource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/roles/{roleId}", JeeRoleServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/roles/{roleId}/users/{userId}", JeeRoleServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/files", JeeFileServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/files/{fileId}", JeeFileServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/{entityType}", JeeEntitiesServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/{entityType}/properties/{propertyName}", JeePropertyServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/{entityType}/{entityId}", JeeEntityServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/{entityType}/{entityId}/blobs", JeeBlobServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/{entityType}/{entityId}/blobs/{blobName}", JeeBlobServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/{entityType}/{entityId}/properties/{propertyName}", JeePropertyServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/{entityType}/{entityId}/links/{linkName}/{targetEntityId}", JeeLinkServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "entities/{entityType}/{entityId}/links/{linkName}", JeeLinksServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "blobs/{blobHash}", JeeBlobHashServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "files", JeeFileServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "files/{fileId}", JeeFileServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "kv/{entityType}", JeeKeyValueServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "kv/{entityType}/{entityId}", JeeKeyValueServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "customCodes/{customCodeName}", JeeCustomCodeServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "customCodes/{customCodeName}/{methodName}", JeeCustomCodeMethodServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "backups", JeeBackupServerResource.class);
    router.attach(DIVROLL_ROOT_URI + "configurations", JeeConfigurationServerResource.class);

    CorsFilter corsFilter = new CorsFilter(getContext());
    corsFilter.setAllowedOrigins(new HashSet(Arrays.asList("*")));
    corsFilter.setSkippingResourceForCorsOptions(true);
    corsFilter.setAllowedHeaders(
        Sets.newHashSet(
            Constants.HEADER_SUPER_AUTH,
            Constants.HEADER_SUPER_AUTH.toLowerCase(),
            Constants.HEADER_MASTER_KEY,
            Constants.HEADER_MASTER_KEY.toLowerCase(),
            Constants.HEADER_API_KEY,
            Constants.HEADER_API_KEY.toLowerCase(),
            Constants.HEADER_APP_ID,
            Constants.HEADER_APP_ID.toLowerCase(),
            Constants.HEADER_NAMESPACE,
            Constants.HEADER_NAMESPACE.toLowerCase(),
            Constants.HEADER_AUTH_TOKEN,
            Constants.HEADER_AUTH_TOKEN.toLowerCase(),
            Constants.HEADER_ACCEPT,
            Constants.HEADER_ACCEPT.toLowerCase(),
            Constants.HEADER_ACL_READ,
            Constants.HEADER_ACL_READ.toLowerCase(),
            Constants.HEADER_ACL_WRITE,
            Constants.HEADER_ACL_WRITE.toLowerCase(),
            Constants.HEADER_CONTENT_TYPE,
            Constants.HEADER_CONTENT_TYPE.toLowerCase()));
    corsFilter.setExposedHeaders(Sets.newHashSet(
            Constants.HEADER_CONTENT_DISPOSITION,
            Constants.HEADER_CONTENT_DISPOSITION.toLowerCase()
    ));
    corsFilter.setAllowedCredentials(true);

    corsFilter.setNext(router);

    FirewallRule rule = new PeriodicFirewallCounterRule(60, TimeUnit.SECONDS, new IpAddressCountingPolicy());
    ((PeriodicFirewallCounterRule)rule).addHandler(new RateLimitationHandler(new UniqueLimitPolicy(rateLimit)));
    FirewallFilter firewallFiler = new FirewallFilter(getContext(), Arrays.asList(rule));
    firewallFiler.setNext(corsFilter);

    return firewallFiler;
  }

  private void configureConverters() {
    List<ConverterHelper> converters = Engine.getInstance().getRegisteredConverters();
    JacksonConverter jacksonConverter = null;
    XStreamConverter xStreamConverter = null;
    for (ConverterHelper converterHelper : converters) {
      if (converterHelper instanceof JacksonConverter) {
        jacksonConverter = (JacksonConverter) converterHelper;
        break;
      } else if (converterHelper instanceof XStreamConverter) {
        xStreamConverter = (XStreamConverter) converterHelper;
      }
    }
    if (jacksonConverter != null) {
      Engine.getInstance().getRegisteredConverters().remove(jacksonConverter);
    }
    if (xStreamConverter != null) {
    };
  }

  private void configureJobScheduler() {
    try {
      Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
      scheduler.start();
      scheduler.resumeAll();
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  private void attachSwaggerSpecification1(Router router) {
    SwaggerSpecificationRestlet swaggerSpecificationRestlet = new SwaggerSpecificationRestlet(
            this);
    swaggerSpecificationRestlet.setBasePath("http://localhost:8080/divroll/api-docs");
    swaggerSpecificationRestlet.attach(router);
  }

  private void attachSwaggerSpecification2(Router router) {
    Swagger2SpecificationRestlet swagger2SpecificationRestlet
            = new Swagger2SpecificationRestlet(this);
    swagger2SpecificationRestlet.setBasePath("http://localhost:8080/divroll/");
    swagger2SpecificationRestlet.attach(router, "/docs");
  }

  private String getBasePath() {
    return "http://localhost:8080/divroll/";
  }

}
