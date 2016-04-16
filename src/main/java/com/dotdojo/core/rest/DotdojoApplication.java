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
package com.divroll.core.rest;

import com.divroll.core.rest.guice.GuiceConfigModule;
import com.divroll.core.rest.guice.SelfInjectingServerResourceModule;
import com.divroll.core.rest.resource.gae.*;
import com.google.inject.Guice;
import com.divroll.core.rest.resource.gae.GaeRootServerResource;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import java.util.logging.Logger;

public class DivrollApplication extends Application {
  private static final Logger LOG
          = Logger.getLogger(DivrollApplication.class.getName());
  private static final String ROOT_URI = "/rest/";
  /**
   * Creates a root Restlet that will receive all incoming calls.
   */
  @Override
  public Restlet createInboundRoot() {
    Guice.createInjector(new GuiceConfigModule(this.getContext()),
            new SelfInjectingServerResourceModule());
    Router router = new Router(getContext());
    router.attach(ROOT_URI + "uploads", GaeUploadServerResource.class);
    router.attachDefault(GaeRootServerResource.class);
    return router;
  }
}
