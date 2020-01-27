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
package com.divroll.core.rest;

import com.divroll.core.rest.guice.GuiceConfigModule;
import com.divroll.core.rest.guice.SelfInjectingServerResourceModule;
import com.divroll.core.rest.resource.JeeRootServerResource;
import com.google.inject.Guice;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
/**
 * Main Application
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.1
 * @since 1.0
 */
public class DivrollApplication extends Application {
  final static Logger LOG
          = LoggerFactory.getLogger(DivrollApplication.class);
  private static final String ROOT_URI = "/";
  /**
   * Creates a root Restlet that will receive all incoming calls.
   */
  @Override
  public Restlet createInboundRoot() {
    Guice.createInjector(new GuiceConfigModule(this.getContext()),
            new SelfInjectingServerResourceModule());
    getRangeService().setEnabled(false);
    Router router = new Router(getContext());
    router.attachDefault(JeeRootServerResource.class);
    return router;
  }
}
