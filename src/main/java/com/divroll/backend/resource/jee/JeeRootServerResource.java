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
package com.divroll.backend.resource.jee;

import com.divroll.backend.guice.SelfInjectingServerResource;
import com.divroll.backend.model.Server;
import com.divroll.backend.resource.RootResource;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Status;
import org.restlet.resource.Get;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeRootServerResource extends SelfInjectingServerResource implements RootResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeRootServerResource.class);

  @Inject
  @Named("app")
  String appName;

  @Inject
  @Named("xodusRoot")
  String xodusRoot;

  @Inject
  @Named("defaultUserStore")
  String defaultUserStore;

  @Get("json")
  public Server represent() {
    Server server = new Server();
    server.setName(appName);
    server.setXodusRoot(xodusRoot);
    server.setdefaultUserStore(defaultUserStore);
    setStatus(Status.SUCCESS_OK);
    return server;
  }
}
