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
package com.divroll.backend.helper;

import com.divroll.backend.model.EmbeddedArrayIterable;
import com.divroll.backend.model.EmbeddedEntityIterable;
import util.ComparableHashMap;
import util.ComparableLinkedList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class Comparables {
  public static <V extends Comparable> ComparableLinkedList<Comparable> cast(
      List<V> comparableList) {
    if (comparableList == null) {
      return null;
    }
    if (comparableList.isEmpty()) {
      return new ComparableLinkedList<Comparable>();
    }
    ComparableLinkedList<Comparable> casted = new ComparableLinkedList<Comparable>();
    comparableList.forEach(
        comparable -> {
          if (comparable instanceof EmbeddedEntityIterable) {
            casted.add(((EmbeddedEntityIterable) comparable).asObject());
          } else if (comparable instanceof EmbeddedArrayIterable) {
            casted.add(cast(((EmbeddedArrayIterable) comparable).asObject()));
          } else {
            casted.add(comparable);
          }
        });
    return casted;
  }

  public static <K extends Comparable, V extends Comparable> ComparableHashMap<K, V> cast(
      Map<K, V> map) {
    if (map == null) {
      return null;
    }
    if (map.isEmpty()) {
      return new ComparableHashMap<>();
    }
    ComparableHashMap<K, V> casted = new ComparableHashMap<>();
    map.forEach(
        (key, value) -> {
          casted.put(key, value);
        });
    return casted;
  }

}
