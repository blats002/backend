package com.divroll.domino.xodus;

import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;

import java.util.LinkedHashMap;
import java.util.Map;

public class XodusManagerImpl implements XodusManager {
    Map<String, Environment> environmentMap = new LinkedHashMap<>();
    Map<String, PersistentEntityStore> entityStoreMap = new LinkedHashMap<>();
    @Override
    public Environment getEnvironment(String xodusRoot, String instance) {
        Environment environment = environmentMap.get(xodusRoot + instance);
        if(environment == null) {
            Environment env = Environments.newInstance(xodusRoot + instance);
            environmentMap.put(xodusRoot + instance, env);
        }
        Environment e = environmentMap.get(xodusRoot + instance);
        return e;
    }

    @Override
    public PersistentEntityStore getPersistentEntityStore(String xodusRoot, String dir) {
        PersistentEntityStore entityStore = entityStoreMap.get(xodusRoot + dir);
        if(entityStore == null) {
            PersistentEntityStore store = PersistentEntityStores.newInstance(xodusRoot + dir);
            entityStoreMap.put(xodusRoot + dir, store);
        }
        PersistentEntityStore p = entityStoreMap.get(xodusRoot + dir);
        return p;
    }
}
