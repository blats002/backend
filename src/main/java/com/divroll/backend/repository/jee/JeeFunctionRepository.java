package com.divroll.backend.repository.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.FunctionRepository;
import com.divroll.backend.util.Base64;
import com.divroll.backend.xodus.XodusStore;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityId;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class JeeFunctionRepository implements FunctionRepository {

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    @Named("masterStore")
    String masterStore;

    @Inject
    XodusStore store;

    @Inject
    EntityRepository entityRepository;

    @Override
    public String createFunction(String appId, String functionName, String jar) {
        Map<String, Comparable> comparableMap = new LinkedHashMap<>();
        comparableMap.put("appId", appId);
        comparableMap.put("functionName", functionName);
        comparableMap.put("jar", jar);
        EntityId entityId = store.putIfNotExists(masterStore, Constants.ENTITYSTORE_FUNCTION, comparableMap, "functionName");
        return entityId.toString();
    }

    @Override
    public boolean deleteFunction(String appId, String functionName) {
        try {
            store.delete(masterStore, Constants.ENTITYSTORE_FUNCTION, "functionName", functionName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public byte[] retrieveFunction(String appId, String functionName) {
        EntityId entityId = store.getFirstEntityId(masterStore, Constants.ENTITYSTORE_FUNCTION, "functionName", functionName, String.class);
        Map<String, Comparable> comparableMap = store.get(masterStore, entityId);
        String jar = (String) comparableMap.get("jar");
        return Base64.base64ToByteArray(jar);
    }

    @Override
    public byte[] retrieveFunctionEntity(String appId, String functionName) {
        byte[] jarBytes = null;
        System.out.println("appId = " + appId);
        InputStream is = entityRepository.getFirstEntityBlob(appId, Constants.ENTITYSTORE_FUNCTION,
                "functionName", functionName, String.class, "jar");
        try {
            jarBytes = ByteStreams.toByteArray(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jarBytes;
    }
}
