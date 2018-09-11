package com.divroll.roll.helper;

import com.divroll.roll.model.EntityStub;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ACLHelper {
    public static boolean validate(com.alibaba.fastjson.JSONArray aclObjects) {
        if(aclObjects != null) {
            for(int i=0;i<aclObjects.size();i++) {
                com.alibaba.fastjson.JSONObject aclObject = aclObjects.getJSONObject(i);
                String entityId = null;
                try {
                    entityId = aclObject.getString("entityId");
                    if(entityId.isEmpty()) {
                        return false;
                    }
                } catch (JSONException e) {
                    return false;
                }

            }
        }
        return true;
    }
    public static String[] onlyIds(List<EntityStub> entityStubs) {
        if(entityStubs == null) {
            return null;
        }
        String[] arr = null;
        int idx = 0;
        for(EntityStub entityStub : entityStubs) {
            String entityId = entityStub.getEntityId();
            assert entityId != null;
            if(entityId != null) {
                if(arr == null) {
                    arr = new String[entityStubs.size()];
                }
                arr[idx] = entityId;
            }
            idx++;
        }
        return arr;
    }
    public static String[] onlyIds(JSONArray aclObjects) {
        if(aclObjects == null) {
            return null;
        }
        List<EntityStub> entityStubs = convert(aclObjects);
        String[] ids = onlyIds(entityStubs);
        return ids;
    }
    public static String[] onlyIds(com.alibaba.fastjson.JSONArray aclObjects) {
        if(aclObjects == null) {
            return null;
        }
        List<EntityStub> entityStubs = convert(aclObjects);
        String[] ids = onlyIds(entityStubs);
        return ids;
    }
    public static List<EntityStub> convert(com.alibaba.fastjson.JSONArray aclObjects) {
        if(aclObjects == null) {
            return null;
        }
        List<EntityStub> entityStubs = null;
        for(int i=0;i<aclObjects.size();i++) {
            if(entityStubs == null) {
                entityStubs = new LinkedList<EntityStub>();
            }
            com.alibaba.fastjson.JSONObject aclObject = aclObjects.getJSONObject(i);
            String entityId = null;
            String entityType = null;
            try {
                entityId = aclObject.getString("entityId");
            } catch (JSONException e) {}
            try {
                entityType = aclObject.getString("entityType");
            } catch (JSONException e) {}
            assert entityId != null;
            EntityStub entityStub = new EntityStub(entityId, entityType);
            entityStubs.add(entityStub);

        }
        return entityStubs;
    }
    public static List<EntityStub> convert(JSONArray aclObjects) {
        if(aclObjects == null) {
            return null;
        }
        List<EntityStub> entityStubs = null;
        for(int i=0;i<aclObjects.length();i++) {
            if(entityStubs == null) {
                entityStubs = new LinkedList<EntityStub>();
            }
            try {
                JSONObject aclObject = aclObjects.getJSONObject(i);
                String entityId = null;
                String entityType = null;
                try {
                    entityId = aclObject.getString("entityId");
                } catch (JSONException e) {}
                try {
                    entityType = aclObject.getString("entityType");
                } catch (JSONException e) {}
                EntityStub entityStub = new EntityStub(entityId, entityType);
                entityStubs.add(entityStub);
            } catch (Exception e) {
                return null;
            }
        }
        return entityStubs;
    }
    public static List<EntityStub> convert(String[] aclArray) {
        if(aclArray == null) {
            return null;
        }
        List<EntityStub> entityStubs = null;
        for(int i=0;i<aclArray.length;i++) {
            if(entityStubs == null) {
                entityStubs = new LinkedList<EntityStub>();
            }
            String entityId = aclArray[i];
            EntityStub entityStub = new EntityStub(entityId);
            entityStubs.add(entityStub);

        }
        return entityStubs;
    }
    public static boolean contains(String entityId, List<EntityStub> entityStubs) {
        if(entityStubs != null) {
            for(EntityStub entityStub : entityStubs) {
                if(entityStub.getEntityId() != null && entityStub.getEntityId().equals(entityId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
