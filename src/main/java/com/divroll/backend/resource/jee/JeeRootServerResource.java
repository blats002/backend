/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
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
@Deprecated
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
