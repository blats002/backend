package com.divroll.backend.service.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.service.PubSubService;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.json.JSONObject;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.apache.http.ObservableHttpResponse;

public class JeePubSubService implements PubSubService {

    // TODO: URI must be based on Restlet server context

    @Override
    public void created(String appId, String entityType, String entityId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
        CloseableHttpAsyncClient httpClient;
        try {
            httpClient = HttpAsyncClients.createDefault();
            httpClient.start();
            Observable<ObservableHttpResponse> observable
                    = ObservableHttp.createRequest(HttpAsyncMethods.createPost("http://localhost:8080/pubsub/"
                            + appId + "/entities/" + entityType + "/created", jsonObject.toString(),
                    ContentType.TEXT_PLAIN), httpClient).toObservable();
            observable.flatMap(response -> response.getContent().map(bytes -> new String(bytes))).subscribe(resp -> {
                System.out.println("ASYNC RESPONSE->" + resp);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updated(String appId, String entityType, String entityId) {
        entityUpdated(appId, entityType, entityId);
    }

    @Override
    public void deleted(String appId, String entityType, String entityId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
        CloseableHttpAsyncClient httpClient;
        try {
            httpClient = HttpAsyncClients.createDefault();
            httpClient.start();
            ObservableHttp.createRequest(HttpAsyncMethods.createPost("http://localhost:8080/pubsub/"
                            + appId + "/entities/" + entityType + "/deleted", jsonObject.toString(),
                    ContentType.TEXT_PLAIN), httpClient).toObservable()
                    .flatMap(response -> response.getContent().map(bytes -> new String(bytes))).subscribe(resp -> {
                System.out.println("ASYNC RESPONSE->" + resp);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deletedAll(String appId, String entityType) {
        CloseableHttpAsyncClient httpClient;
        try {
            httpClient = HttpAsyncClients.createDefault();
            httpClient.start();
            ObservableHttp.createRequest(HttpAsyncMethods.createPost("http://localhost:8080/pubsub/"
                            + appId + "/entities/" + entityType + "/deletedAll", new JSONObject().toString(),
                    ContentType.TEXT_PLAIN), httpClient).toObservable()
                    .flatMap(response -> response.getContent().map(bytes -> new String(bytes))).subscribe(resp -> {
                System.out.println("ASYNC RESPONSE->" + resp);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void linked(String appId, String entityType, String linkName, String entityId, String targetEntityId) {
        entityUpdated(appId, entityType, entityId);
    }

    @Override
    public void unlinked(String appId, String entityType, String linkName, String entityId, String targetEntityId) {
        entityUpdated(appId, entityType, entityId);
    }

    private void entityUpdated(String appId, String entityType, String entityId) {
        appId = "TEST";
//        System.out.println("appId->" + appId);
//        System.out.println("entityType->" + entityType);
//        System.out.println("entityId->" + entityId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
        CloseableHttpAsyncClient httpClient;
        try {
            httpClient = HttpAsyncClients.createDefault();
            httpClient.start();
            ObservableHttp.createRequest(HttpAsyncMethods.createPost("http://localhost:8080/pubsub/"
                            + appId + "/entities/" + entityType + "/updated", jsonObject.toString(),
                    ContentType.TEXT_PLAIN), httpClient).toObservable()
                    .flatMap(response -> response.getContent().map(bytes -> new String(bytes))).subscribe(resp -> {
                System.out.println("ASYNC RESPONSE->" + resp);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
