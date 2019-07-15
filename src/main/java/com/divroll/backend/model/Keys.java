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
package com.divroll.backend.model;

import com.divroll.backend.Constants;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class Keys {
  public static boolean isReservedPropertyKey(String propertyKey) {
    if (propertyKey.equals(Constants.RESERVED_FIELD_ENTITY_ID)) {
      return true;
    } else if (propertyKey.equals(Constants.RESERVED_FIELD_ACL_READ)) {
      return true;
    } else if (propertyKey.equals(Constants.RESERVED_FIELD_ACL_WRITE)) {
      return true;
    } else if (propertyKey.equals(Constants.RESERVED_FIELD_LINKNAMES)) {
      return true;
    } else if (propertyKey.equals(Constants.RESERVED_FIELD_BLOBNAMES)) {
      return true;
    } else if (propertyKey.equals(Constants.RESERVED_FIELD_PUBLICREAD)) {
      return true;
    } else if (propertyKey.equals(Constants.RESERVED_FIELD_PUBLICWRITE)) {
      return true;
    } else if (propertyKey.equals(Constants.RESERVED_FIELD_METADATA)) {
      return true;
    }
    return false;
  }
}
