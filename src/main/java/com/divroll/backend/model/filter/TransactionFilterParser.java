package com.divroll.backend.model.filter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TransactionFilterParser {

    List<TransactionFilter> filters = new LinkedList<>();

//    private static TransactionFilterParser INSTANCE = null;

//    public static TransactionFilterParser get() {
//        if(INSTANCE == null) {
//            INSTANCE = new TransactionFilterParser();
//        }
//        return INSTANCE;
//    }

    public List<TransactionFilter> parseQuery(String query) {
        if(query == null) {
            return null;
        }
        JSONObject queryObj = new JSONObject(query);
        queryObj.keySet().forEach(key -> {
            if(key.equals("$find")) {
                JSONObject findObj = queryObj.getJSONObject(key);
                if(findObj == null) {
                    throw new IllegalArgumentException("Invalid value for " + key);
                }
                processFindObj(null, filters, findObj, TransactionFilter.EQUALITY_OP.EQUAL);
            } else if(key.equals("$findStartingWith")) {
                JSONObject findStartsWithObj = queryObj.getJSONObject(key);
                if(findStartsWithObj == null) {
                    throw new IllegalArgumentException("Invalid value for " + key);
                }
                processFindObj(null, filters, findStartsWithObj, TransactionFilter.EQUALITY_OP.STARTS_WITH);
            }
        });
        Collections.reverse(filters);
        return filters;
    }

    private void processFindObj(TransactionFilter.BINARY_OP parentOp, List<TransactionFilter> filters, JSONObject findObj,
                                TransactionFilter.EQUALITY_OP equalityOp) {
        findObj.keySet().forEach(key -> {
            if(key.startsWith("$intersect")) {
                JSONObject intersectObj = findObj.getJSONObject(key);
                intersectObj.keySet().forEach(s -> {
                    if(s.startsWith("$find")) {
                        processFindObj(TransactionFilter.BINARY_OP.INTERSECT, filters, intersectObj.getJSONObject(s), TransactionFilter.EQUALITY_OP.EQUAL);
                    } else if(s.startsWith("$findStartingWith")) {
                        processFindObj(TransactionFilter.BINARY_OP.INTERSECT, filters, intersectObj.getJSONObject(s), TransactionFilter.EQUALITY_OP.STARTS_WITH);
                    }
                });
            } else if(key.startsWith("$union")) {
                JSONObject unionObj = findObj.getJSONObject(key);
                unionObj.keySet().forEach(s -> {
                    if(s.startsWith("$find")) {
                        processFindObj(TransactionFilter.BINARY_OP.UNION, filters, unionObj.getJSONObject(s), TransactionFilter.EQUALITY_OP.EQUAL);
                    } else if(s.startsWith("$findStartingWith")) {
                        processFindObj(TransactionFilter.BINARY_OP.UNION, filters, unionObj.getJSONObject(s), TransactionFilter.EQUALITY_OP.STARTS_WITH);
                    }
                });
            } else if(key.startsWith("$minus")) {
                JSONObject minusObj = findObj.getJSONObject(key);
                minusObj.keySet().forEach(s -> {
                    if(s.startsWith("$find")) {
                        processFindObj(TransactionFilter.BINARY_OP.MINUS, filters, minusObj.getJSONObject(s), TransactionFilter.EQUALITY_OP.EQUAL);
                    } else if(s.startsWith("$findStartingWith")) {
                        processFindObj(TransactionFilter.BINARY_OP.MINUS, filters, minusObj.getJSONObject(s), TransactionFilter.EQUALITY_OP.STARTS_WITH);
                    }
                });
            } else if(key.startsWith("$concat")) {
                JSONObject concatObj = findObj.getJSONObject(key);
                concatObj.keySet().forEach(s -> {
                    if(s.startsWith("$find")) {
                        processFindObj(TransactionFilter.BINARY_OP.CONCAT, filters, concatObj.getJSONObject(s), TransactionFilter.EQUALITY_OP.EQUAL);
                    } else if(s.startsWith("$findStartingWith")) {
                        processFindObj(TransactionFilter.BINARY_OP.CONCAT, filters, concatObj.getJSONObject(s), TransactionFilter.EQUALITY_OP.STARTS_WITH);
                    }
                });
            } else if(key.startsWith("$find")) {
                // TODO: Not reached
                 processFindObj(parentOp, filters, findObj.getJSONObject(key), TransactionFilter.EQUALITY_OP.EQUAL);
            } else if(key.startsWith("$findStartingWith")) {
                // TODO: Not reached
                processFindObj(parentOp, filters, findObj.getJSONObject(key), TransactionFilter.EQUALITY_OP.STARTS_WITH);
            } else {
                // should be a property name followed by values
                try {
                    Object propertyValue = findObj.get(key);
                    if(propertyValue instanceof JSONObject) {
                        Object minValue = ((JSONObject) propertyValue).get("minValue");
                        Object maxValue = ((JSONObject) propertyValue).get("maxValue");
                        if(minValue == null) {
                            throw new IllegalArgumentException("Invalid null value for " + key);
                        }
                        if(maxValue == null) {
                            throw new IllegalArgumentException("Invalid null value for " + key);
                        }
                        if(!minValue.getClass().getName().equals(maxValue.getClass().getName())) {
                            throw new IllegalArgumentException("minValue and maxValue should have the same class type");
                        }
                        Comparable comparableMinValue = (Comparable) minValue;
                        Comparable comparableMaxValue = (Comparable) maxValue;
                        TransactionFilter queryFilter = new TransactionFilter(parentOp, key, comparableMinValue, comparableMaxValue, equalityOp);
                        filters.add(queryFilter);
                    } else if(propertyValue instanceof JSONArray) {
                        throw new IllegalArgumentException("Invalid value type JSONArray for " + key);
                    } else if(propertyValue instanceof Boolean) {
                        Boolean value = Boolean.valueOf((Boolean) propertyValue);
                        TransactionFilter filter = new TransactionFilter(parentOp, key, value, equalityOp);
                        filters.add(filter);
                    } else if(propertyValue instanceof Number) {
                        Number value = (Number) propertyValue;
                        Double doubleValue = value.doubleValue();
                        TransactionFilter filter = new TransactionFilter(parentOp, key, doubleValue, equalityOp);
                        filters.add(filter);
                    } else if(propertyValue instanceof String) {
                        String value = (String) propertyValue;
                        TransactionFilter filter = new TransactionFilter(parentOp, key, value, equalityOp);
                        filters.add(filter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
   }

}
