package com.divroll.backend.service;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface PubSubService {
    void created(String appId, String namespace, String entityType, String entityId);
    void updated(String appId, String namespace, String entityType, String entityId);
    void deleted(String appId, String namespace, String entityType, String entityId);
    void deletedAll(String appId, String namespace, String entityType);
    void linked(String appId, String namespace, String entityType, String linkName, String entityId, String targetEntityId);
    void unlinked(String appId, String namespace, String entityType, String linkName, String entityId, String targetEntityId);
}
