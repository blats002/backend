package com.divroll.backend.pubsub;

import com.divroll.backend.Constants;
import com.divroll.backend.model.EmbeddedArrayIterable;
import com.divroll.backend.model.EmbeddedEntityBinding;
import com.divroll.backend.model.EmbeddedEntityIterable;
import jetbrains.exodus.entitystore.*;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereInterceptorAdapter;
import org.atmosphere.cpr.AtmosphereResource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AuthenticationInterceptor extends AtmosphereInterceptorAdapter {

    private final static Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Override
    public Action inspect(AtmosphereResource r) {
        logger.info("AuthenticationInterceptor - inspect method called");
        if (r.getRequest().getMethod().equalsIgnoreCase("POST")) {
            String masterKey = r.getRequest().getHeader(Constants.HEADER_MASTER_KEY);
            String appId = r.getRequest().getRequestURI().split("/")[2];
            if(!isMaster(appId, masterKey)) {
                return Action.CANCELLED;
            }
        } else if(r.getRequest().getMethod().equalsIgnoreCase("GET")) {
            String apiKey = r.getRequest().getHeader(Constants.HEADER_API_KEY);
            String appId = r.getRequest().getRequestURI().split("/")[2];
            if(!isAuthorized(appId, apiKey)) {
                return Action.CANCELLED;
            }
        }
        return Action.CONTINUE;
    }

    private boolean isMaster(String appId, String masterKey) {
        logger.info("appId->" + appId);
        logger.info("masterKey->" + masterKey);
        Map<String,Comparable> comparableMap = get(appId);
        if(comparableMap != null) {
            String entityId = (String) comparableMap.get("entityId");
            logger.info("entityId->" + entityId);
        }
        return false;
    }

    private boolean isAuthorized(String appId, String apiKey) {
        logger.info("appId->" + appId);
        logger.info("apiKey->" + apiKey);
        Map<String,Comparable> comparableMap = get(appId);
        if(comparableMap != null) {
            String entityId = (String) comparableMap.get("entityId");
            logger.info("entityId->" + entityId);
        }
        return false;
    }

    public Map<String, Comparable> get(final String id) {
        Properties properties = readProperties();
        String xodusRoot = properties.getProperty("xodusRoot");
        String masterStore = properties.getProperty("masterStore");
        System.out.println("xodusRoot->" + xodusRoot);
        System.out.println("masterStore->" + masterStore);
        final Map<String, Comparable>[] result = new Map[]{null};
        final PersistentEntityStore entityStore = getPersistentEntityStore(xodusRoot, masterStore);
        try {
            entityStore.executeInReadonlyTransaction(new StoreTransactionalExecutable() {
                @Override
                public void execute(@NotNull StoreTransaction txn) {
                    Entity entity = txn.find(Constants.ENTITYSTORE_APPLICATION, Constants.APP_ID, id).getFirst();
                    if(entity != null) {
                        result[0] = new LinkedHashMap<>();
                        List<String> props = entity.getPropertyNames();
                        for (String prop : props) {
                            result[0].put(prop, entity.getProperty(prop));
                        }
                    }
                }
            });
        } finally {
            //entityStore.close();
        }
        return result[0];
    }

    protected PersistentEntityStore getPersistentEntityStore(String xodusRoot, String dir) {
        final PersistentEntityStore store = PersistentEntityStores.newInstance(xodusRoot + dir);
        store.executeInReadonlyTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull StoreTransaction txn) {
                store.registerCustomPropertyType(txn, EmbeddedEntityIterable.class, EmbeddedEntityBinding.BINDING);
                store.registerCustomPropertyType(txn, EmbeddedArrayIterable.class, EmbeddedEntityBinding.BINDING);
            }
        });
        return store;
    }

    protected Properties readProperties() {
        InputStream is = this.getClass().getResourceAsStream("/app.properties");
        Properties props = new Properties();
        try {
            props.load(is);
            return props;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
