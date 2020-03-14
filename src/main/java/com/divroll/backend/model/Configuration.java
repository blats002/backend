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
package com.divroll.backend.model;

import org.immutables.value.Value;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface Configuration {

  String getXodusRoot();

  String getDefaultUserStore();

  String getDefaultRoleStore();

  String getMasterStore();

  String getFileStore();

  //    private final String xodusRoot;
  //    private final String defaultUserStore;
  //    private final String defaultRoleStore;
  //    private final String masterStore;
  //    private final String fileStore;

  //    public Configuration(String xodusRoot,
  //                         String defaultUserStore,
  //                         String defaultRoleStore,
  //                         String masterStore,
  //                         String fileStore) {
  //        this.xodusRoot = xodusRoot;
  //        this.defaultUserStore = defaultUserStore;
  //        this.defaultRoleStore = defaultRoleStore;
  //        this.masterStore = masterStore;
  //        this.fileStore = fileStore;
  //    }

}
