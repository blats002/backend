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
package com.divroll.core.rest;

import com.divroll.core.rest.guice.GuiceConfigModule;
import com.divroll.core.rest.guice.SelfInjectingServerResourceModule;
import com.divroll.core.rest.resource.GaeRootServerResource;
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
 * @version 1.0
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
    router.attachDefault(GaeRootServerResource.class);
    return router;
  }
}
