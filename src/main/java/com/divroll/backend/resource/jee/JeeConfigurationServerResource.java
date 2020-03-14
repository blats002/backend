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
