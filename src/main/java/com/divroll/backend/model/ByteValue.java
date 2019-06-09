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

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class ByteValue implements Serializable, Comparable<byte[]>, ACL {

  private byte[] value;

  // Read and write ACL
  private String[] read;
  private String[] write;

  public String[] getRead() {
    return read;
  }

  public void setRead(String[] read) {
    this.read = read;
  }

  public String[] getWrite() {
    return write;
  }

  public void setWrite(String[] write) {
    this.write = write;
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }

  @Override
  public int compareTo(@NotNull byte[] o) {
    return ByteBuffer.wrap(value).compareTo(ByteBuffer.wrap(o));
  }
}
