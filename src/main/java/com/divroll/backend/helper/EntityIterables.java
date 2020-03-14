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
package com.divroll.backend.helper;

import com.divroll.backend.model.EmbeddedArrayIterable;
import com.divroll.backend.model.EmbeddedEntityIterable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EntityIterables {
  public static JSONObject toJSONObject(EmbeddedEntityIterable embeddedEntityIterable) {
    if (embeddedEntityIterable != null) {
      Map<String, Comparable> comparableMap =
          (Map<String, Comparable>) embeddedEntityIterable.asObject();
      JSONObject jsonObject = new JSONObject();
      comparableMap.forEach(
          (key, value) -> {
            if (value instanceof EmbeddedArrayIterable) {
              JSONObject jso = toJSONObject((EmbeddedEntityIterable) value);
              jsonObject.put(key, jso);
            } else if (value instanceof EmbeddedEntityIterable) {
              JSONArray jsa = toJSONArray((EmbeddedArrayIterable) value);
              jsonObject.put(key, jsa);
            } else {
              jsonObject.put(key, value);
            }
          });
      return jsonObject;
    }
    return null;
  }

  public static JSONArray toJSONArray(EmbeddedArrayIterable embeddedArrayIterable) {
    if (embeddedArrayIterable != null) {
      List<Comparable> comparableList = embeddedArrayIterable.asObject();
      JSONArray jsonArray = new JSONArray();
      comparableList.forEach(
          value -> {
            if (value instanceof EmbeddedArrayIterable) {
              JSONArray jso = toJSONArray((EmbeddedArrayIterable) value);
              jsonArray.put(jso);
            } else if (value instanceof EmbeddedEntityIterable) {
              JSONObject jsa = toJSONObject((EmbeddedEntityIterable) value);
              jsonArray.put(jsa);
            } else {
              jsonArray.put(value);
            }
          });
      return jsonArray;
    }
    return null;
  }
}
