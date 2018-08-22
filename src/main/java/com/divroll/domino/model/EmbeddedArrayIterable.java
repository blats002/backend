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
package com.divroll.domino.model;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.ByteIterator;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EmbeddedArrayIterable implements Serializable, ByteIterable {

    private byte[] bytes;

    public EmbeddedArrayIterable(JSONArray jsonArray) {
        try {
            bytes = jsonArray.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
        }
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

    public JSONArray asJSONArray() {
        try {
            String jsonString = new String(bytes, "utf-8");
            return new JSONArray(jsonString);
        } catch (UnsupportedEncodingException e) {

        }
        return null;
    }



}
