/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.backend.resource.jee;

import com.alibaba.fastjson.JSONObject;
import com.divroll.backend.Constants;
import com.divroll.backend.guice.SelfInjectingServerResource;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.action.Action;
import com.divroll.backend.model.action.ActionParser;
import com.divroll.backend.model.filter.TransactionFilter;
import com.divroll.backend.model.filter.TransactionFilterParser;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.service.ApplicationService;
import com.divroll.backend.service.EntityService;
import com.divroll.backend.service.SchemaService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.collect.Sets;
import com.google.common.escape.Escapers;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;
import scala.actors.threadpool.Arrays;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class BaseServerResource extends SelfInjectingServerResource {

  protected static final Integer DEFAULT_LIMIT = 100;
  private static final Logger LOG = LoggerFactory.getLogger(BaseServerResource.class);
  protected Map<String, Comparable> queryMap = new LinkedHashMap<>();
  protected Map<String, String> propsMap = new LinkedHashMap<>();

  protected String appName;
  protected String entityId;
  protected String entityType;
  protected String blobName;
  protected String appId;
  protected String namespace;
  protected String apiKey;
  protected String masterKey;
  protected String authToken;
  protected String aclRead;
  protected String aclWrite;
  protected String accept;
  protected String contentType;
  protected String userId;
  protected String username;
  protected String roleId;
  protected Integer skip = null;
  protected Integer limit = null;
  protected String sort;
  protected Boolean publicRead;
  protected Boolean publicWrite;

  protected String propertyName;

  protected String linkName;
  protected String backlinkName;
  protected String targetEntityId;
  protected String linkFrom;
  protected String linkTo;

  protected String masterToken;

  protected List<TransactionFilter> filters;
  protected List<Action> actions;
  protected String fileName;
  protected List<String> uniqueProperties;
  @Inject ApplicationService applicationService;

  @Inject EntityRepository entityRepository;

  @Inject SchemaService schemaService;

  @Inject EntityService entityService;
  private Application application;

  @Override
  protected void doInit() {
    super.doInit();
    filters = new LinkedList<>();
    Series<Header> responseHeaders =
        (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
    if (responseHeaders == null) {
      responseHeaders = new Series(Header.class);
      getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
    }
    responseHeaders.add(new Header("X-Powered-By", Constants.SERVER_NAME));
    setAllowedMethods(
        Sets.newHashSet(Method.GET, Method.PUT, Method.POST, Method.DELETE, Method.OPTIONS));
    propsMap = appProperties();
    entityId = getAttribute(Constants.RESERVED_FIELD_ENTITY_ID);
    entityType = getAttribute(Constants.ENTITY_TYPE);
    blobName = getAttribute("blobName");
    userId = getAttribute(Constants.USER_ID);
    roleId = getAttribute(Constants.ROLE_ID);
    propertyName = getAttribute("propertyName");
    linkName = getAttribute("linkName");
    targetEntityId = getAttribute("targetEntityId");

    if (linkName == null) {
      linkName = getQueryValue("linkName");
    }

    backlinkName = getQueryValue("backlinkName");

    linkFrom = getQueryValue("linkFrom");
    linkTo = getQueryValue("linkTo");

    username = getQueryValue(Constants.QUERY_USERNAME);
    sort = getQueryValue("sort");

    Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");

    LOG.with(headers).info("Logging headers");

    namespace =
        headers.getFirstValue("X-Divroll-Namespace") != null
            ? headers.getFirstValue("X-Divroll-Namespace")
            : headers.getFirstValue("X-Divroll-Namespace".toLowerCase());

    appId =
        headers.getFirstValue(Constants.HEADER_APP_ID) != null
            ? headers.getFirstValue(Constants.HEADER_APP_ID)
            : headers.getFirstValue(Constants.HEADER_APP_ID.toLowerCase());
    apiKey =
        headers.getFirstValue(Constants.HEADER_API_KEY) != null
            ? headers.getFirstValue(Constants.HEADER_API_KEY)
            : headers.getFirstValue(Constants.HEADER_API_KEY.toLowerCase());
    masterKey =
        headers.getFirstValue(Constants.HEADER_MASTER_KEY) != null
            ? headers.getFirstValue(Constants.HEADER_MASTER_KEY)
            : headers.getFirstValue(Constants.HEADER_MASTER_KEY.toLowerCase());
    authToken =
        headers.getFirstValue(Constants.HEADER_AUTH_TOKEN) != null
            ? headers.getFirstValue(Constants.HEADER_AUTH_TOKEN)
            : headers.getFirstValue(Constants.HEADER_AUTH_TOKEN.toLowerCase());
    masterToken =
        headers.getFirstValue(Constants.HEADER_MASTER_TOKEN) != null
            ? headers.getFirstValue(Constants.HEADER_MASTER_TOKEN)
            : headers.getFirstValue(Constants.HEADER_MASTER_TOKEN.toLowerCase());

    if (appId == null) {
      appId = getQueryValue(Constants.APP_ID);
    }

    if (apiKey == null) {
      apiKey = getQueryValue(Constants.API_KEY);
    }

    if (masterKey == null) {
      masterKey = getQueryValue(Constants.MASTER_KEY);
    }

    if (namespace == null) {
      namespace = getQueryValue("namespace");
    }

    aclRead =
        headers.getFirstValue(Constants.HEADER_ACL_READ) != null
            ? headers.getFirstValue(Constants.HEADER_ACL_READ)
            : headers.getFirstValue(Constants.HEADER_ACL_READ.toLowerCase());
    aclWrite =
        headers.getFirstValue(Constants.HEADER_ACL_WRITE) != null
            ? headers.getFirstValue(Constants.HEADER_ACL_WRITE)
            : headers.getFirstValue(Constants.HEADER_ACL_WRITE.toLowerCase());
    accept =
        headers.getFirstValue(Constants.HEADER_ACCEPT) != null
            ? headers.getFirstValue(Constants.HEADER_ACCEPT)
            : headers.getFirstValue(Constants.HEADER_ACCEPT.toLowerCase());
    contentType =
        headers.getFirstValue(Constants.HEADER_CONTENT_TYPE) != null
            ? headers.getFirstValue(Constants.HEADER_CONTENT_TYPE)
            : headers.getFirstValue(Constants.HEADER_CONTENT_TYPE.toLowerCase());

      LOG.info("Captured aclWrite - " + aclWrite);
      LOG.info("Captured aclRead  - " + aclRead);
    try {
      aclWrite = URLDecoder.decode(aclWrite, "UTF-8");
      aclRead = URLDecoder.decode(aclRead, "UTF-8");
    } catch (Exception e) {
    }

    LOG.info("Decoded captured aclWrite - " + aclWrite);
    LOG.info("Decoded captured aclRead  - " + aclRead);
    appName = getAttribute("appName");

    try {
      skip = Integer.valueOf(getQueryValue(Constants.QUERY_SKIP));
      limit = Integer.valueOf(getQueryValue(Constants.QUERY_LIMIT));
    } catch (Exception e) {
      // do nothing
    }

    if (skip == null) {
      skip = 0;
    }

    if (limit == null) {
      limit = DEFAULT_LIMIT;
    }

    try {
      publicRead = Boolean.valueOf(getQueryValue("publicRead"));
    } catch (Exception e) {

    }

    try {
      publicWrite = Boolean.valueOf(getQueryValue("publicWrite"));
    } catch (Exception e) {

    }

    if (appId != null) {
      application = applicationService.read(appId);
    }

    String queries = getQueryValue("queries");
    if (queries != null) {
      try {
        filters = new TransactionFilterParser().parseQuery(queries);
      } catch (Exception e) {

      }
    }

    String actionsString = getQueryValue("actions");
    if (actionsString != null) {
      try {
        actions = new ActionParser().parseAction(actionsString);
        LOG.with(actions).info("Actions: " + actions.size());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    fileName = getAttribute("fileName");

    String uniquePropertiesString = getQueryValue("uniqueProperties");
    if (uniquePropertiesString != null) {
      JSONArray jsonArray = new JSONArray(uniquePropertiesString);
      if (jsonArray != null) {
        for (int i = 0; i < jsonArray.length(); i++) {
          String uniqueProperty = jsonArray.getString(i);
          if (uniqueProperties == null) {
            uniqueProperties = new LinkedList<>();
          }
          uniqueProperties.add(uniqueProperty);
        }
      }
    }
  }

  protected Map<String, String> appProperties() {
    Map<String, String> map = new LinkedHashMap<String, String>();
    InputStream is = getContext().getClass().getResourceAsStream("/app.properties");
    Properties props = new Properties();
    try {
      props.load(is);
      map = new LinkedHashMap<String, String>((Map) props);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return map;
  }

  protected byte[] toByteArray(Object object) throws IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos)) {
      out.writeObject(object);
      return bos.toByteArray();
    }
  }

  protected Object fromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = new ObjectInputStream(bis)) {
      return in.readObject();
    }
  }

  protected boolean isAuthorized() {
    if (apiKey == null || apiKey.isEmpty()) {
      return false;
    }
    if (application != null) {
      if (BCrypt.checkpw(masterKey, application.getMasterKey())) {
        return true;
      }
      if (BCrypt.checkpw(apiKey, application.getApiKey())) {
        return true;
      }
    }
    return false;
  }

  protected boolean isMaster() {
    if (application != null && (BCrypt.checkpw(masterKey, application.getMasterKey()))) {
      return true;
    }
    return false;
  }

  protected Representation returnNull() {
    JSONObject jsonObject = new JSONObject();
    return new JsonRepresentation(jsonObject.toJSONString());
  }

  protected Representation returnServerError() {
    JSONObject jsonObject = new JSONObject();
    setStatus(Status.SERVER_ERROR_INTERNAL);
    return new JsonRepresentation(jsonObject.toJSONString());
  }

  protected Representation returnMissingAuthToken() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("error", Constants.ERROR_MISSING_AUTH_TOKEN);
    return new JsonRepresentation(jsonObject.toJSONString());
  }

  protected Representation missingUsernamePasswordPair() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("error", Constants.ERROR_MISSING_USERNAME_PASSWORD);
    return new JsonRepresentation(jsonObject.toJSONString());
  }

  protected Map<String, Comparable> cleanup(Map<String, Comparable> result) {
    result.remove(Constants.RESERVED_FIELD_PUBLICWRITE);
    result.remove(Constants.RESERVED_FIELD_PUBLICREAD);
    return result;
  }

  protected boolean validateId(String id) {
    return (id != null && !id.isEmpty() && !id.equalsIgnoreCase("null"));
  }

  protected void validateIds(String[] read, String[] write) throws IllegalArgumentException {
    if (read != null) {
      List<String> idList = Arrays.asList(read);
      for (String id : idList) {
        if (id == null) {
          throw new IllegalArgumentException();
        }
        if (id.isEmpty()) {
          throw new IllegalArgumentException();
        }
      }
    }
    if (write != null) {
      List<String> idList = Arrays.asList(write);
      for (String id : idList) {
        if (id == null) {
          throw new IllegalArgumentException();
        }
        if (id.isEmpty()) {
          throw new IllegalArgumentException();
        }
      }
    }
  }

  protected Application getApp() {
    return application;
  }

  public boolean beforeSave(
      Application application, Map<String, Comparable> entity, String appId, String entityType) {
    return entityService.beforeSave(application, namespace, entity, appId, entityType);
  }

  public boolean afterSave(
      Application application, Map<String, Comparable> entity, String appId, String entityType) {
    return entityService.afterSave(application, namespace, entity, appId, entityType);
  }

  public boolean beforeSave(Map<String, Comparable> entity, String appId, String entityType) {
    return entityService.beforeSave(getApp(), namespace, entity, appId, entityType);
  }

  public boolean afterSave(Map<String, Comparable> entity, String appId, String entityType) {
    return entityService.afterSave(getApp(), namespace, entity, appId, entityType);
  }

  protected Representation notFound() {
    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
    return null;
  }

  protected Representation unauthorized() {
    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    return null;
  }

  protected Representation badRequest() {
    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    return null;
  }

  protected Representation badRequest(String message) {
    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    return new StringRepresentation(message);
  }

  protected Representation serverError() {
    setStatus(Status.SERVER_ERROR_INTERNAL);
    return null;
  }

  protected Representation success(org.json.JSONObject jsonObject) {
    setStatus(Status.SUCCESS_OK);
    if (jsonObject == null) {
      return null;
    }
    Representation representation = new JsonRepresentation(jsonObject.toString());
    return representation;
  }

  protected Representation success() {
    setStatus(Status.SUCCESS_OK);
    return null;
  }

  protected Representation created(org.json.JSONObject jsonObject) {
    setStatus(Status.SUCCESS_CREATED);
    if (jsonObject == null) {
      return null;
    }
    Representation representation = new JsonRepresentation(jsonObject.toString());
    return representation;
  }

  //    protected Representation success() {
  //        JSONObject jsonObject = new JSONObject();
  //        jsonObject.put("success", true);
  //        jsonObject.put("code", Status.SUCCESS_OK.getCode());
  //        jsonObject.put("error", Status.SUCCESS_OK.getReasonPhrase());
  //        Representation response = new StringRepresentation(jsonObject.toJSONString());
  //        response.setMediaType(MediaType.APPLICATION_JSON);
  //        setStatus(Status.SUCCESS_OK);
  //        return response;
  //    }
  //
  //    protected Representation success(int code, String reasonPhrase) {
  //        JSONObject jsonObject = new JSONObject();
  //        jsonObject.put("success", true);
  //        jsonObject.put("code", code);
  //        jsonObject.put("error", reasonPhrase);
  //        Representation response = new StringRepresentation(jsonObject.toJSONString());
  //        response.setMediaType(MediaType.APPLICATION_JSON);
  //        setStatus(Status.SUCCESS_OK);
  //        return response;
  //    }
  //
  //    protected Representation success(int code) {
  //        JSONObject jsonObject = new JSONObject();
  //        jsonObject.put("success", true);
  //        jsonObject.put("code", code);
  //        jsonObject.put("error", Status.SUCCESS_OK.getReasonPhrase());
  //        Representation response = new StringRepresentation(jsonObject.toJSONString());
  //        response.setMediaType(MediaType.APPLICATION_JSON);
  //        setStatus(Status.SUCCESS_OK);
  //        return response;
  //    }

  //    protected Representation badRequest() {
  //        JSONObject jsonObject = new JSONObject();
  //        jsonObject.put("success", false);
  //        jsonObject.put("code", Status.CLIENT_ERROR_BAD_REQUEST.getCode());
  //        jsonObject.put("error", Status.CLIENT_ERROR_BAD_REQUEST.getReasonPhrase());
  //        Representation response = new StringRepresentation(jsonObject.toJSONString());
  //        response.setMediaType(MediaType.APPLICATION_JSON);
  //        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
  //        return response;
  //    }

  //    protected Representation badRequest(String message) {
  //        JSONObject jsonObject = new JSONObject();
  //        jsonObject.put("success", false);
  //        jsonObject.put("code", Status.CLIENT_ERROR_BAD_REQUEST.getCode());
  //        jsonObject.put("error", message);
  //        Representation response = new StringRepresentation(jsonObject.toJSONString());
  //        response.setMediaType(MediaType.APPLICATION_JSON);
  //        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
  //        return response;
  //    }
  //
  //    protected Representation internalError() {
  //        JSONObject jsonObject = new JSONObject();
  //        jsonObject.put("success", false);
  //        jsonObject.put("code", Status.SERVER_ERROR_INTERNAL.getCode());
  //        jsonObject.put("error", Status.SERVER_ERROR_INTERNAL.getReasonPhrase());
  //        Representation response = new StringRepresentation(jsonObject.toJSONString());
  //        response.setMediaType(MediaType.APPLICATION_JSON);
  //        setStatus(Status.SERVER_ERROR_INTERNAL);
  //        return response;
  //    }

  protected Representation internalError(String stacktrace) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("success", false);
    jsonObject.put("code", Status.SERVER_ERROR_INTERNAL.getCode());
    jsonObject.put("error", Status.SERVER_ERROR_INTERNAL.getReasonPhrase());
    jsonObject.put("stacktrace", stacktrace);
    Representation response = new StringRepresentation(jsonObject.toJSONString());
    response.setMediaType(MediaType.APPLICATION_JSON);
    setStatus(Status.SERVER_ERROR_INTERNAL);
    return response;
  }

  //    protected Representation unauthorized() {
  //        JSONObject jsonObject = new JSONObject();
  //        jsonObject.put("code", Status.CLIENT_ERROR_UNAUTHORIZED.getCode());
  //        jsonObject.put("error", Status.CLIENT_ERROR_UNAUTHORIZED.getReasonPhrase());
  //        Representation response = new StringRepresentation(jsonObject.toJSONString());
  //        response.setMediaType(MediaType.APPLICATION_JSON);
  //        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
  //        return response;
  //    }

  //    protected Representation notFound() {
  //        JSONObject jsonObject = new JSONObject();
  //        jsonObject.put("code", Status.CLIENT_ERROR_NOT_FOUND.getCode());
  //        jsonObject.put("error", Status.CLIENT_ERROR_NOT_FOUND.getReasonPhrase());
  //        Representation response = new StringRepresentation(jsonObject.toJSONString());
  //        response.setMediaType(MediaType.APPLICATION_JSON);
  //        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
  //        return response;
  //    }

}
