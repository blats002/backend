package com.divroll.roll.xodus;

import com.divroll.roll.model.EmbeddedArrayIterable;
import com.divroll.roll.model.EmbeddedEntityBinding;
import com.divroll.roll.model.EmbeddedEntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.entitystore.StoreTransaction;
import jetbrains.exodus.entitystore.StoreTransactionalExecutable;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class XodusManagerImpl implements XodusManager {
    Map<String, Environment> environmentMap = new LinkedHashMap<>();
    Map<String, PersistentEntityStore> entityStoreMap = new LinkedHashMap<>();

    @Override
    public Environment getEnvironment(String xodusRoot, String instance) {
        Environment environment = environmentMap.get(xodusRoot + instance);
        if (environment == null) {
            Environment env = Environments.newInstance(xodusRoot + instance);
            environmentMap.put(xodusRoot + instance, env);
        }
        Environment e = environmentMap.get(xodusRoot + instance);
        return e;
    }

    @Override
    public PersistentEntityStore getPersistentEntityStore(String xodusRoot, String dir) {
        PersistentEntityStore entityStore = entityStoreMap.get(xodusRoot + dir);
        if (entityStore == null) {
            final PersistentEntityStore store = PersistentEntityStores.newInstance(xodusRoot + dir);
            store.executeInTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull StoreTransaction txn) {
                    store.registerCustomPropertyType(txn, EmbeddedEntityIterable.class, EmbeddedEntityBinding.BINDING);
                    store.registerCustomPropertyType(txn, EmbeddedArrayIterable.class, EmbeddedEntityBinding.BINDING);
                }
            });
            entityStoreMap.put(xodusRoot + dir, store);
        }
        PersistentEntityStore p = entityStoreMap.get(xodusRoot + dir);
        return p;
    }
}
