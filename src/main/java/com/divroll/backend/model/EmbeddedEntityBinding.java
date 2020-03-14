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
package com.divroll.backend.model;

import com.google.common.io.ByteStreams;
import jetbrains.exodus.bindings.ComparableBinding;
import jetbrains.exodus.util.LightOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EmbeddedEntityBinding extends ComparableBinding {

  public static final EmbeddedEntityBinding BINDING = new EmbeddedEntityBinding();

  public static byte[] serialize(Object obj) {
    try {
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
          ObjectOutput out = new ObjectOutputStream(bos)) {
        out.writeObject(obj);
        return bos.toByteArray();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static <T> T deserialize(byte[] data, Class<T> clazz) {
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(data);
      ObjectInputStream is = new ObjectInputStream(in);
      return (T) is.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Comparable readObject(@NotNull ByteArrayInputStream stream) {
    try {
      byte[] serialized = ByteStreams.toByteArray(stream);
      Comparable deserialized = deserialize(serialized, Comparable.class);
      return deserialized;
    } catch (Exception e) {

    }
    return null;
  }

  @Override
  public void writeObject(@NotNull LightOutputStream output, @NotNull Comparable object) {
    byte[] serialized = serialize(object);
    output.write(serialized);
  }
}
