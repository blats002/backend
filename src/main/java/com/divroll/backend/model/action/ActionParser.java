/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
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
package com.divroll.backend.model.action;

import com.divroll.backend.helper.JSON;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class ActionParser {
  List<Action> actionList = new LinkedList<Action>();

  public List<Action> parseAction(String action) {
    if (action == null) {
      return null;
    }
    JSONObject actionObj = new JSONObject(action);
    actionObj
        .keySet()
        .forEach(
            key -> {
              if (key.equals("$link")) {
                JSONObject linkObj = actionObj.getJSONObject(key);
                if (linkObj == null) {
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
    if (op == null) {
      return null;
    }
    if (op.equals(Action.ACTION_OP.SET)) {
      String propertyName = operatorObj.getString("propertyName");
      Object propertyValue = operatorObj.get("propertyValue");
      if (propertyValue instanceof JSONObject) {
        JSONObject propertyValueObj = (JSONObject) propertyValue;
        JSONObject referenceProperty = propertyValueObj.getJSONObject("$ref");
        String referencePropertyName = referenceProperty.getString("propertyName");
        Action action =
            new ActionBuilder()
                .actionOp(op)
                .propertyName(propertyName)
                .referenceProperty(referencePropertyName)
                .build();
        return action;
      }

    } else if (op.equals(Action.ACTION_OP.LINK)) {
      List<Action> subActions = new LinkedList<>();
      Map<String, Comparable> entityMap = jsonObjToMap(operatorObj.getJSONObject("entity"));
      Action action =
          new ActionBuilder()
              .actionOp(Action.ACTION_OP.LINK)
              .entityType(operatorObj.getString("entityType"))
              .entity(entityMap)
              .linkName(operatorObj.getString("linkName"))
              .backLinkName(operatorObj.getString("backLinkName"))
              .build();
      operatorObj
          .keySet()
          .forEach(
              key -> {
                if (key.startsWith("$")) {
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
    if (key.equals("$link")) {
      return Action.ACTION_OP.LINK;
    } else if (key.equals("$set")) {
      return Action.ACTION_OP.SET;
    }
    return null;
  }

  private Map<String, Comparable> jsonObjToMap(JSONObject jsonObject) {
    Map<String, Comparable> comparableMap = JSON.jsonToMap(jsonObject);
    return comparableMap;
  }
}
