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
import jetbrains.exodus.util.ByteIterableUtil;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class ByteValueIterable implements ByteIterable {

  private final long offset;
  private final int length;
  private ByteValue byteValue;
  private byte[] bytes;

  public ByteValueIterable(ByteValue byteValue) {
    byte[] bytes = toByteArray(byteValue);
    this.bytes = bytes;
    this.byteValue = byteValue;
    this.offset = 0L;
    this.length = (int) bytes.length;
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
    return length;
  }

  @NotNull
  @Override
  public ByteIterable subIterable(int offset, int length) {
    throw new UnsupportedOperationException("subIterable");
  }

  @Override
  public int compareTo(@NotNull ByteIterable o) {
    return ByteIterableUtil.compare(this, o);
  }

  protected byte[] toByteArray(Object object) {
    try {
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
          ObjectOutput out = new ObjectOutputStream(bos)) {
        out.writeObject(object);
        return bos.toByteArray();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  };
}
