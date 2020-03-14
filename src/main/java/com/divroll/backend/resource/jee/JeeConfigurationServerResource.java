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
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * GPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.resource.jee;

import com.divroll.backend.model.Configuration;
import com.divroll.backend.model.ConfigurationBuilder;
import com.divroll.backend.resource.ConfigurationResource;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeConfigurationServerResource extends BaseServerResource
    implements ConfigurationResource {

  @Inject
  @Named("masterToken")
  String theMasterToken;

  @Inject
  @Named("xodusRoot")
  String xodusRoot;

  @Inject
  @Named("defaultUserStore")
  String defaultUserStore;

  @Inject
  @Named("defaultRoleStore")
  String defaultRoleStore;

  @Inject
  @Named("masterStore")
  String masterStore;

  @Inject
  @Named("fileStore")
  String fileStore;

  @Override
  public Configuration getConfiguration() {
    try {
      if (theMasterToken != null
          && masterToken != null
          && BCrypt.checkpw(masterToken, theMasterToken)) {
        Configuration configuration =
            new ConfigurationBuilder()
                .xodusRoot(xodusRoot)
                .defaultUserStore(defaultUserStore)
                .defaultRoleStore(defaultRoleStore)
                .masterStore(masterStore)
                .fileStore(fileStore)
                .build();
        setStatus(Status.SUCCESS_OK);
        return configuration;
      } else {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      }
    } catch (Exception e) {
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return null;
  }
}
