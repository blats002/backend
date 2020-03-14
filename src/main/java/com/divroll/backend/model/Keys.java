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
