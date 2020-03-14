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
import org.json.JSONException;
import org.json.JSONObject;
import util.ComparableHashMap;
import util.ComparableLinkedList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JSON {
  private JSON() {}

  public static Map<String, Comparable> jsonToMap(JSONObject json) throws JSONException {
    Map<String, Comparable> retMap = new ComparableHashMap<String, Comparable>();
    if (json != JSONObject.NULL) {
      retMap = toMap(json);
    }
    return retMap;
  }

  public static Map<String, Comparable> toMap(JSONObject object) throws JSONException {
    Map<String, Comparable> map = new ComparableHashMap<String, Comparable>();
    Iterator<String> keysItr = object.keys();
    while (keysItr.hasNext()) {
      String key = keysItr.next();
      Object value = object.get(key);
      if (value instanceof JSONArray) {
        List<Comparable> valueList = toList((JSONArray) value);
        EmbeddedArrayIterable iterable = new EmbeddedArrayIterable(valueList);
        map.put(key, iterable);
      } else if (value instanceof JSONObject) {
        Map<String, Comparable> valueMap = toMap((JSONObject) value);
        EmbeddedEntityIterable iterable = new EmbeddedEntityIterable(Comparables.cast(valueMap));
        map.put(key, iterable);
      } else if (value != JSONObject.NULL) {
        map.put(key, (Comparable) value);
      }
    }
    return map;
  }

  public static List<Comparable> toList(JSONArray array) throws JSONException {
    List<Comparable> list = new ComparableLinkedList<>();
    for (int i = 0; i < array.length(); i++) {
      Object value = array.get(i);
      if (value instanceof JSONArray) {
        List<Comparable> valueList = toList((JSONArray) value);
        value = new EmbeddedArrayIterable(valueList);
      } else if (value instanceof JSONObject) {
        Map<String, Comparable> valueMap = toMap((JSONObject) value);
        value = new EmbeddedEntityIterable(Comparables.cast(valueMap));
      }
      list.add((Comparable) value);
    }
    return list;
  }
}
