package com.divroll.backend.service;

public interface PubSubService {
    void created(String appId, String entityType, String entityId);
    void updated(String appId, String entityType, String entityId);
    void deleted(String appId, String entityType, String entityId);
    void deletedAll(String appId, String entityType);
    void linked(String appId, String entityType, String linkName, String entityId, String targetEntityId);
    void unlinked(String appId, String entityType, String linkName, String entityId, String targetEntityId);
}
