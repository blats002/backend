package com.divroll.backend.model.action;

import com.divroll.backend.helper.JSON;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ActionParser {
    List<Action> actionList = new LinkedList<Action>();
    public List<Action> parseAction(String action) {
        if(action == null) {
            return null;
        }
        JSONObject actionObj = new JSONObject(action);
        actionObj.keySet().forEach(key -> {
            if(key.equals("$link")) {
                JSONObject linkObj = actionObj.getJSONObject(key);
                if(linkObj == null) {
                    throw new IllegalArgumentException("Invalid value for " + key);
                }
                Action act = processOperatorObj(Action.ACTION_OP.LINK, linkObj);
                actionList.add(act);
            }
        });
        Collections.reverse(actionList);
        return actionList;
    }

    private Action processOperatorObj(Action.ACTION_OP op, JSONObject operatorObj) {
        if(op == null) {
            return null;
        }
        if(op.equals(Action.ACTION_OP.SET)) {
            String propertyName = operatorObj.getString("propertyName");
            Object propertyValue = operatorObj.get("propertyValue");
            if(propertyValue instanceof JSONObject) {
                JSONObject propertyValueObj = (JSONObject) propertyValue;
                JSONObject referenceProperty = propertyValueObj.getJSONObject("$ref");
                String referencePropertyName = referenceProperty.getString("propertyName");
                Action action = new ActionBuilder()
                        .actionOp(op)
                        .propertyName(propertyName)
                        .referenceProperty(referencePropertyName).build();
                return action;
            }

        } else if(op.equals(Action.ACTION_OP.LINK)) {
            List<Action> subActions = new LinkedList<>();
            Map<String,Comparable> entityMap = jsonObjToMap(operatorObj.getJSONObject("entity"));
            Action action = new ActionBuilder()
                    .actionOp(Action.ACTION_OP.LINK)
                    .entityType(operatorObj.getString("entityType"))
                    .entity(entityMap)
                    .linkName(operatorObj.getString("linkName"))
                    .backLinkName(operatorObj.getString("backLinkName")).build();
            operatorObj.keySet().forEach(key -> {
                if(key.startsWith("$")) {
                    JSONObject suboperatorObj = operatorObj.getJSONObject(key);
                    Action subAction = processOperatorObj(actionOp(key), suboperatorObj);
                    subActions.add(subAction);
                }
            });
            return new ActionBuilder().from(action).next(subActions.iterator().next()).build();
        }
        return null;
    }

    private Action.ACTION_OP actionOp(String key) {
        if(key.equals("$link")) {
            return Action.ACTION_OP.LINK;
        } else if(key.equals("$set")) {
            return Action.ACTION_OP.SET;
        }
        return null;
    }

    private Map<String,Comparable> jsonObjToMap(JSONObject jsonObject) {
//        Type mapType = new TypeToken<Map<String, Comparable>>(){}.getType();
//        Map<String, Comparable> comparableMap = new Gson().fromJson(jsonObject.toString(), mapType);
//        return comparableMap;
        Map<String, Comparable> comparableMap = JSON.toComparableMap(jsonObject);
        return comparableMap;
    }

    private Comparable toComparable(Object jsonValue) {
        if(jsonValue instanceof JSONObject) {
            // TODO
        } else if(jsonValue instanceof JSONArray) {
           // TODO
        } else if(jsonValue instanceof Boolean) {
            Boolean value = Boolean.valueOf((Boolean) jsonValue);
            return value;
        } else if(jsonValue instanceof Number) {
            Number value = (Number) jsonValue;
            Double doubleValue = value.doubleValue();
            return doubleValue;
        } else if(jsonValue instanceof String) {
            String value = (String) jsonValue;
            return value;
        }
        return null;
    }

}
