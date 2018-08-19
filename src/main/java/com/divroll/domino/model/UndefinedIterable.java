package com.divroll.domino.model;

import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.ByteIterator;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class UndefinedIterable implements Serializable, ByteIterable {

    private byte[] bytes;

    public UndefinedIterable() {
        bytes = "null".getBytes();
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
}
