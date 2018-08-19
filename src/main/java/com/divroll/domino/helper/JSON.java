package com.divroll.domino.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class JSON {
    public static Map<String,Comparable> toComparableMap(JSONObject jsonObject) {
        Iterator<String> it = jsonObject.keySet().iterator();
        Map<String, Comparable> comparableMap = new LinkedHashMap<>();
        while (it.hasNext()) {
            String k = it.next();
            try {
                JSONObject jso = jsonObject.getJSONObject(k);
                // TODO
            } catch (Exception e) {

            }
            try {
                JSONArray jsa = jsonObject.getJSONArray(k);
                // TODO
            } catch (Exception e) {

            }
            try {
                Boolean value = jsonObject.getBoolean(k);
                comparableMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                Long value = jsonObject.getLong(k);
                comparableMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                Double value = jsonObject.getDouble(k);
                comparableMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                String value = jsonObject.getString(k);
                comparableMap.put(k, value);
            } catch (Exception e) {

            }
        }
        return comparableMap;
    }
}
