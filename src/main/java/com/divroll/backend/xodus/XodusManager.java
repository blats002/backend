package com.divroll.backend.xodus;

import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.env.Environment;

public interface XodusManager {
    public Environment getEnvironment(String xodusRoot, String instance);

    public PersistentEntityStore getPersistentEntityStore(String xodusRoot, String dir);
}
