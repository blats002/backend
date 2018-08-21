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
package com.divroll.domino.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JSON {
    private JSON() {}
    public static Map<String,Comparable> toComparableMap(JSONObject jsonObject) {
        Iterator<String> it = jsonObject.keySet().iterator();
        Map<String, Comparable> comparableMap = new LinkedHashMap<>();
        while (it.hasNext()) {
            String k = it.next();
            try {
                JSONObject jso = jsonObject.getJSONObject(k);
                // TODO
            } catch (Exception e) {

            }
            try {
                JSONArray jsa = jsonObject.getJSONArray(k);
                // TODO
            } catch (Exception e) {

            }
            try {
                Boolean value = jsonObject.getBoolean(k);
                comparableMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                Long value = jsonObject.getLong(k);
                comparableMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                Double value = jsonObject.getDouble(k);
                comparableMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                String value = jsonObject.getString(k);
                comparableMap.put(k, value);
            } catch (Exception e) {

            }
        }
        return comparableMap;
    }
}
