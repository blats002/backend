package com.divroll.backend.trigger;

import com.divroll.backend.repository.jee.AppEntityRepository;

import java.util.Map;

public class TriggerRequest {

    private String entityType;
    private Map<String, Comparable> entity;
    private AppEntityRepository query;

    private TriggerRequest() {}

    public TriggerRequest(Map<String,Comparable> entity, String entityType, AppEntityRepository query) {
        setEntity(entity);
        setQuery(query);
        setEntityType(entityType);
    }

    public Map<String, Comparable> getEntity() {
        return entity;
    }

    public void setEntity(Map<String, Comparable> entity) {
        this.entity = entity;
    }

    public AppEntityRepository getQuery() {
        return query;
    }

    public void setQuery(AppEntityRepository query) {
        this.query = query;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}
