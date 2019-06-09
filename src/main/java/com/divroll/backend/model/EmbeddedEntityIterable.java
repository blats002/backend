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
package com.divroll.backend.model;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.ByteIterator;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EmbeddedEntityIterable implements Serializable, ByteIterable {

  private static final long serialVersionUID = 9074087750155200888L;

  private byte[] bytes;

  public EmbeddedEntityIterable(Comparable object) {
    bytes = serialize(object);
  }

  public static byte[] serialize(Object obj) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(out);
      os.writeObject(obj);
      return out.toByteArray();
    } catch (IOException e) {
    }
    return null;
  }

  public static Comparable deserialize(byte[] data) {
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(data);
      ObjectInputStream is = new ObjectInputStream(in);
      return (Comparable) is.readObject();
    } catch (IOException e) {
    } catch (ClassNotFoundException e) {
    }
    return null;
  }

  @Override
  public ByteIterator iterator() {
    return new ArrayByteIterable(bytes).iterator();
  }

  @Override
  public byte[] getBytesUnsafe() {
    return bytes;
  }

  @Override
  public int getLength() {
    return bytes.length;
  }

  @NotNull
  @Override
  public ByteIterable subIterable(int offset, int length) {
    return null;
  }

  @Override
  public int compareTo(@NotNull ByteIterable o) {
    return 0;
  }

  public Comparable asObject() {
    return deserialize(bytes);
  }
}
