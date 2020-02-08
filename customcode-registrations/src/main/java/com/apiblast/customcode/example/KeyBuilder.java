package com.apiblast.customcode.example;

import java.util.LinkedList;
import java.util.List;

public class KeyBuilder {
    public final static String KEY_SEPARATOR = ":";
    List<String> keys = new LinkedList<String>();
    public KeyBuilder key(String key) {
        keys.add(key);
        return this;
    }
    public String get() {
        String key = "";
        for(String s : keys) {
            key = key + KEY_SEPARATOR;
        }
        // Remove the last key separator
        key = key.substring(0, key.length() - 1);
        return key;
    }
}
