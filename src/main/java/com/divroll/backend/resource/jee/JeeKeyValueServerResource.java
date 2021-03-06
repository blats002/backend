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
package com.divroll.backend.resource.jee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.divroll.backend.helper.ACLHelper;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.exception.ACLException;
import com.divroll.backend.resource.KeyValueResource;
import com.divroll.backend.service.KeyValueService;
import com.divroll.backend.service.WebTokenService;
import com.divroll.backend.xodus.XodusEnvStore;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeKeyValueServerResource extends BaseServerResource implements KeyValueResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeKeyValueServerResource.class);

  @Inject KeyValueService keyValueService;

  @Inject WebTokenService webTokenService;

  @Inject XodusEnvStore store;

  @Override
  public Representation getValue() {
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }
      if (authToken == null || authToken.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }

      Application app = applicationService.read(appId);
      if (app == null) {
        return null;
      }

      String authUsername = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      String uuidKey = "username:" + authUsername + ":uuid";
      String authUUID = store.get(appId, authUsername, uuidKey, String.class);

      String dir = appId;
      if (dir != null) {
        if (accept != null) {
          if (accept.equals(MediaType.APPLICATION_OCTET_STREAM.toString())
              || accept.equals("application/octet-stream")) {
            ByteBuffer value =
                keyValueService.get(
                    appId, namespace, entityType, entityId, authUUID, ByteBuffer.class);
            if (value != null) {
              byte[] arr = new byte[value.remaining()];
              value.get(arr);
              Representation representation = new ByteArrayRepresentation(arr);
              representation.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
              setStatus(Status.SUCCESS_OK);
              return representation;
            }
          } else if (accept.equals(MediaType.APPLICATION_JSON.toString())) {
            String value =
                keyValueService.get(appId, namespace, entityType, entityId, authUUID, String.class);
            if (value != null) {
              Representation representation = new JsonRepresentation(value);
              setStatus(Status.SUCCESS_OK);
              return representation;
            } else {
              return null;
            }
          } else {
            // default
            String value =
                keyValueService.get(appId, namespace, entityType, entityId, authUUID, String.class);
            if (value != null) {
              Representation representation = new StringRepresentation(value);
              setStatus(Status.SUCCESS_OK);
              return representation;
            } else {
              return null;
            }
          }
        } else {
          // treat as String
          String value =
              keyValueService.get(appId, namespace, entityType, entityId, authUUID, String.class);
          boolean success =
              keyValueService.delete(appId, namespace, entityType, entityId, authUUID);
          if (success) {
            Representation representation = new StringRepresentation(value);
            setStatus(Status.SUCCESS_OK);
            return representation;
          } else {
            setStatus(Status.SERVER_ERROR_INTERNAL);
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return null;
  }

  @Override
  public Representation createValue(Representation entity) {
    JSONObject result = new JSONObject();
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }
      if (entity == null || entity.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      }

      String[] read = new String[] {};
      String[] write = new String[] {};

      if (aclRead != null) {
        try {
          JSONArray jsonArray = JSONArray.parseArray(aclRead);
          read = ACLHelper.onlyIds(jsonArray);
        } catch (Exception e) {
          // do nothing
        }
      }

      if (aclWrite != null) {
        try {
          JSONArray jsonArray = JSONArray.parseArray(aclWrite);
          List<String> aclWriteList = new LinkedList<>();
          for (int i = 0; i < jsonArray.size(); i++) {
            aclWriteList.add(jsonArray.getString(i));
          }
          write = aclWriteList.toArray(new String[aclWriteList.size()]);
        } catch (Exception e) {
          // do nothing
        }
      }

      String dir = appId;
      if (dir != null) {
        if (contentType.equals(MediaType.APPLICATION_OCTET_STREAM.toString())
            || contentType.equals("application/octet-stream")) {
          byte[] value = ByteStreams.toByteArray(entity.getStream());
          boolean success =
              keyValueService.putIfNotExists(
                  appId,
                  namespace,
                  entityType,
                  entityId,
                  ByteBuffer.wrap(value),
                  read,
                  write,
                  ByteBuffer.class);
          if (success) {
            setStatus(Status.SUCCESS_CREATED);
          } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          }
        } else {
          String value = entity.getText();
          boolean success =
              keyValueService.putIfNotExists(
                  appId, namespace, entityType, entityId, value, read, write, String.class);
          if (success) {
            setStatus(Status.SUCCESS_CREATED);
          } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          }
        }
      }
    } catch (ACLException e) {
      e.printStackTrace();
      setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    Representation representation = new JsonRepresentation(result.toJSONString());
    return representation;
  }

  @Override
  public Representation updateValue(Representation entity) {
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }
      if (entity == null || entity.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      }
      if (authToken == null || authToken.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return returnMissingAuthToken();
      }

      Application app = applicationService.read(appId);
      if (app == null) {
        return returnNull();
      }

      String[] read = new String[] {};
      String[] write = new String[] {};

      if (aclRead != null) {
        try {
          JSONArray jsonArray = JSONArray.parseArray(aclRead);
          read = ACLHelper.onlyIds(jsonArray);
        } catch (Exception e) {
          // do nothing
        }
      }

      if (aclWrite != null) {
        try {
          JSONArray jsonArray = JSONArray.parseArray(aclWrite);
          List<String> aclWriteList = new LinkedList<>();
          for (int i = 0; i < jsonArray.size(); i++) {
            aclWriteList.add(jsonArray.getString(i));
          }
          write = aclWriteList.toArray(new String[aclWriteList.size()]);
        } catch (Exception e) {
          // do nothing
        }
      }

      String authUsername = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      String uuidKey = "username:" + authUsername + ":uuid";
      String authUUID = store.get(appId, authUsername, uuidKey, String.class);

      String dir = appId;
      if (dir != null) {
        if (contentType.equals(MediaType.APPLICATION_OCTET_STREAM.toString())
            || contentType.equals("application/octet-stream")) {
          byte[] value = ByteStreams.toByteArray(entity.getStream());
          boolean success =
              keyValueService.put(
                  appId,
                  namespace,
                  entityType,
                  entityId,
                  ByteBuffer.wrap(value),
                  authUUID,
                  read,
                  write,
                  ByteBuffer.class);
          if (success) {
            setStatus(Status.SUCCESS_NO_CONTENT);
            return null;
          } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
          }
        } else {
          String value = entity.getText();
          try {
            // Try to put the value, if it throws then this is a unauthorized request
            boolean success =
                keyValueService.put(
                    appId,
                    namespace,
                    entityType,
                    entityId,
                    value,
                    authUUID,
                    read,
                    write,
                    String.class);
            if (success) {
              setStatus(Status.SUCCESS_NO_CONTENT);
              return null;
            } else {
              setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
              return null;
            }
          } catch (ACLException e) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
          }
        }
      }

    } catch (ACLException e) {
      setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return new StringRepresentation("");
  }

  @Override
  public void deleteValue(Representation entity) {
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return;
      }
      if (entity == null || entity.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      }
      if (authToken == null || authToken.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return;
      }

      Application app = applicationService.read(appId);
      if (app == null) {
        return;
      }

      String authUsername = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      String uuidKey = "username:" + authUsername + ":uuid";
      String authUUID = store.get(appId, authUsername, uuidKey, String.class);

      String dir = appId;
      if (dir != null) {
        try {
          // Try to put the value, if it throws then this is a unauthorized request
          boolean success =
              keyValueService.delete(appId, namespace, entityType, entityId, authUUID);
          if (success) {
            setStatus(Status.SUCCESS_OK);
          } else {
            setStatus(Status.SERVER_ERROR_INTERNAL);
          }
        } catch (ACLException e) {
          setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
  }
}
