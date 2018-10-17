package com.divroll.backend.repository.jee;

import com.divroll.backend.repository.EntityRepository;

import java.util.List;
import java.util.Map;

public class AppEntityRepository {

    private final EntityRepository repository;
    private final String instance;
    private final String storeName;

    public AppEntityRepository(EntityRepository repository, String instance, String storeName) {
        this.repository = repository;
        this.storeName = storeName;
        this.instance = instance;
    }

    public Map<String,Object> getEntityById(String entityId) {
        return repository.getEntity(instance, storeName, entityId);
    }

    public boolean isExist(String entityType, String propertyName, Comparable propertyValue) {
        List<Map<String,Object>> entities = repository.getEntities(instance, entityType, propertyName, propertyValue, 0, 1);
        return !entities.isEmpty();
    }

}
