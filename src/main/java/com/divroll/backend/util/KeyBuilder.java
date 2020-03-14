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

package com.divroll.backend.util;

import java.util.LinkedList;
import java.util.List;

public class KeyBuilder {
    public final static String KEY_SEPARATOR = ":";
    List<String> keys = new LinkedList<String>();
    public KeyBuilder key(String key) {
        keys.add(key);
        return this;
    }
    public String get() {
        String key = "";
        for(String s : keys) {
            key = key + KEY_SEPARATOR;
        }
        // Remove the last key separator
        key = key.substring(0, key.length() - 1);
        return key;
    }
}
