/**
 *
 * Copyright (c) 2014 Kerby Martino and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.divroll.webdash.server;

import com.divroll.webdash.server.resource.BlogResource;
import com.divroll.webdash.server.resource.gae.*;
import com.divroll.webdash.server.service.gae.GaeFileService;
import com.google.inject.Guice;
import com.divroll.webdash.server.guice.GuiceConfigModule;
import com.divroll.webdash.server.guice.SelfInjectingServerResourceModule;
import com.divroll.webdash.server.resource.RootServerResource;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

import java.util.logging.Logger;

public class WebdashApplication extends Application {
  private static final Logger LOG
          = Logger.getLogger(WebdashApplication.class.getName());
  private static final String ROOT_URI = "/rest/";
  /**
   * Creates a root Restlet that will receive all incoming calls.
   */
  @Override
  public Restlet createInboundRoot() {
    Guice.createInjector(new GuiceConfigModule(this.getContext()),
            new SelfInjectingServerResourceModule());
    Router router = new Router(getContext());
    Directory directory = new Directory(getContext(), "war:///doc");
    directory.setIndexName("app.html");
    router.attach(ROOT_URI + "blogs", GaeBlogsServerResource.class);
    router.attach(ROOT_URI + "blogs/{blog_id}", GaeBlogServerResource.class);
    router.attach(ROOT_URI + "files", GaeFilesServerResource.class);
    router.attach(ROOT_URI + "files/{file_id}", GaeFileServerResource.class);
    router.attach(ROOT_URI + "users", GaeUsersServerResource.class);
    router.attach(ROOT_URI + "users/{user_id}", GaeUserServerResource.class);
    router.attach(ROOT_URI + "values", GaeValuesServerResource.class);
    router.attach(ROOT_URI + "values/{value_id}", GaeValueServerResource.class);
    router.attach("/webdash/", directory);
    router.attach("/", RootServerResource.class);
    return router;
  }
}