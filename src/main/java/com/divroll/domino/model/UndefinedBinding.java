package com.divroll.domino.model;

import com.google.common.io.ByteStreams;
import jetbrains.exodus.bindings.ComparableBinding;
import jetbrains.exodus.util.LightOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class UndefinedBinding extends ComparableBinding {

    public static final UndefinedBinding BINDING = new UndefinedBinding();

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

}
