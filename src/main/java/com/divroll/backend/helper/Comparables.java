/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019, Divroll, and individual contributors
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
