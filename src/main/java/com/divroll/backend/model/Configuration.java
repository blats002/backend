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
package com.divroll.backend.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
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
