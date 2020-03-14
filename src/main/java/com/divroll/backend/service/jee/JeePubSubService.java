/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.service.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.service.PubSubService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.json.JSONObject;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.apache.http.ObservableHttpResponse;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeePubSubService implements PubSubService {

  private static final Logger LOG = LoggerFactory.getLogger(JeePubSubService.class);

  // TODO: URI must be based on Restlet server context
  @Inject
  @Named("pubSubBase")
  String pubSubBase;

  @Override
  public void created(String appId, String namespace, String entityType, String entityId) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
    CloseableHttpAsyncClient httpClient;
    try {
      httpClient = HttpAsyncClients.createDefault();
      httpClient.start();
      Observable<ObservableHttpResponse> observable =
          ObservableHttp.createRequest(
                  HttpAsyncMethods.createPost(
                      pubSubBase + appId + "/entities/" + entityType + "/created",
                      jsonObject.toString(),
                      ContentType.TEXT_PLAIN),
                  httpClient)
              .toObservable();
      observable
          .flatMap(response -> response.getContent().map(bytes -> new String(bytes)))
          .subscribe(
              resp -> {
                LOG.info("PubSub response: " + resp);
              });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void updated(String appId, String namespace, String entityType, String entityId) {
    entityUpdated(appId, entityType, entityId);
  }

  @Override
  public void deleted(String appId, String namespace, String entityType, String entityId) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
    CloseableHttpAsyncClient httpClient;
    try {
      httpClient = HttpAsyncClients.createDefault();
      httpClient.start();
      ObservableHttp.createRequest(
              HttpAsyncMethods.createPost(
                  "http://localhost:8080/pubsub/" + appId + "/entities/" + entityType + "/deleted",
                  jsonObject.toString(),
                  ContentType.TEXT_PLAIN),
              httpClient)
          .toObservable()
          .flatMap(response -> response.getContent().map(bytes -> new String(bytes)))
          .subscribe(
              resp -> {
                LOG.info("PubSub response: " + resp);
              });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deletedAll(String appId, String namespace, String entityType) {
    CloseableHttpAsyncClient httpClient;
    try {
      httpClient = HttpAsyncClients.createDefault();
      httpClient.start();
      ObservableHttp.createRequest(
              HttpAsyncMethods.createPost(
                  "http://localhost:8080/pubsub/"
                      + appId
                      + "/entities/"
                      + entityType
                      + "/deletedAll",
                  new JSONObject().toString(),
                  ContentType.TEXT_PLAIN),
              httpClient)
          .toObservable()
          .flatMap(response -> response.getContent().map(bytes -> new String(bytes)))
          .subscribe(
              resp -> {
                LOG.info("PubSub response: " + resp);
              });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void linked(
      String appId,
      String namespace,
      String entityType,
      String linkName,
      String entityId,
      String targetEntityId) {
    entityUpdated(appId, entityType, entityId);
  }

  @Override
  public void unlinked(
      String appId,
      String namespace,
      String entityType,
      String linkName,
      String entityId,
      String targetEntityId) {
    entityUpdated(appId, entityType, entityId);
  }

  private void entityUpdated(String appId, String entityType, String entityId) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
    CloseableHttpAsyncClient httpClient;
    try {
      httpClient = HttpAsyncClients.createDefault();
      httpClient.start();
      ObservableHttp.createRequest(
              HttpAsyncMethods.createPost(
                  "http://localhost:8080/pubsub/" + appId + "/entities/" + entityType + "/updated",
                  jsonObject.toString(),
                  ContentType.TEXT_PLAIN),
              httpClient)
          .toObservable()
          .flatMap(response -> response.getContent().map(bytes -> new String(bytes)))
          .subscribe(
              resp -> {
                LOG.info("PubSub response: " + resp);
              });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
