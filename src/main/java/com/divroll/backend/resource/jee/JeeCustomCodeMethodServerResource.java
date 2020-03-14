/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
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
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * GPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.resource.jee;

import com.alibaba.fastjson.JSON;
import com.divroll.backend.customcode.MethodVerb;
import com.divroll.backend.customcode.rest.CustomCodeRequest;
import com.divroll.backend.customcode.rest.CustomCodeResponse;
import com.divroll.backend.customcodes.CustomCode;
import com.divroll.backend.repository.CustomCodeRepository;
import com.divroll.backend.resource.CustomCodeMethodResource;
import com.divroll.backend.service.CacheService;
import com.divroll.backend.util.StringUtil;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteStreams;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.restlet.Message;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeCustomCodeMethodServerResource extends BaseServerResource
    implements CustomCodeMethodResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeApplicationServerResource.class);
  private static final String HEADERS_KEY = "org.restlet.http.headers";
  private static final int DEFAULT_TIMEOUT = 1000;

  @Inject
  CustomCodeRepository customCodeRepository;

  @Inject
  CacheService cacheService;

  String customCodeName;
  String methodName;
  int customCodeTimeout = DEFAULT_TIMEOUT;

  @Override
  protected void doInit() {
    super.doInit();
    customCodeName = getAttribute("customCodeName");
    methodName = getAttribute("methodName");
    String CUSTOM_CODE_TIMEOUT = System.getenv("CUSTOM_CODE_TIMEOUT");
    if(CUSTOM_CODE_TIMEOUT != null && !CUSTOM_CODE_TIMEOUT.isEmpty()) {
      customCodeTimeout = Integer.valueOf(CUSTOM_CODE_TIMEOUT);
    }
    customCodeTimeout = 1000;
  }

  private InputStream getJar(String appId, String customCodeName) {
    InputStream jarBytes = customCodeRepository.getCustomCode(appId, namespace, customCodeName);
    return jarBytes;
  }

  private void customCodeGet(
          InputStream jarBytes,
      String path,
      Map<String, String> params,
      InputStream body,
      String methodName,
      int counter,
      CompletableFuture<CustomCodeResponse> future) throws IOException {
    CustomCodeRequest request =
        new CustomCodeRequest(MethodVerb.GET, path, params, body, methodName, counter);
    CustomCode customCode = new CustomCode(ByteStreams.toByteArray(jarBytes), future);
    customCode.executeMainClass(request, customCodeTimeout);
  }

  private void customCodePost(
          InputStream jarBytes,
      String path,
      Map<String, String> params,
          InputStream body,
      String methodName,
      int counter,
      CompletableFuture<CustomCodeResponse> future) throws IOException {
    CustomCodeRequest request =
        new CustomCodeRequest(MethodVerb.POST, path, params, body, methodName, counter);
    CustomCode customCode = new CustomCode(ByteStreams.toByteArray(jarBytes), future);
    customCode.executeMainClass(request, customCodeTimeout);
  }

  private void customCodePut(
          InputStream jarBytes,
      String path,
      Map<String, String> params,
          InputStream body,
      String methodName,
      int counter,
      CompletableFuture<CustomCodeResponse> future) throws IOException {
    CustomCodeRequest request =
        new CustomCodeRequest(MethodVerb.PUT, path, params, body, methodName, counter);
    CustomCode customCode = new CustomCode(ByteStreams.toByteArray(jarBytes), future);
    customCode.executeMainClass(request, customCodeTimeout);
  }

  private void customCodeDelete(
          InputStream jarBytes,
      String path,
      Map<String, String> params,
          InputStream body,
      String methodName,
      int counter,
      CompletableFuture<CustomCodeResponse> future) throws IOException {
    CustomCodeRequest request =
        new CustomCodeRequest(MethodVerb.DELETE, path, params, body, methodName, counter);
    CustomCode customCode = new CustomCode(ByteStreams.toByteArray(jarBytes), future);
    customCode.executeMainClass(request, customCodeTimeout);
  }

  protected String stackTraceToString(Throwable e) {
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement element : e.getStackTrace()) {
      sb.append(element.toString());
      sb.append("\n");
    }
    return sb.toString();
  }

  @Override
  public Representation getMethod(Representation entity) {
    if (!isAuthorized()) {
      return unauthorized();
    }
    LOG.info("CustomCode Name: " + customCodeName);
    LOG.info("CustomCOde Method: " + methodName);

    if(getCached()) {
      return cachedRepresentation();
    }

    InputStream jarBytes = getJar(appId, customCodeName);

    if (jarBytes == null) {
      return notFound();
    }
    byte[] content = null;
    InputStream is = null;
    try {
      is = entity != null ? entity.getStream() : null;
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      if (jarBytes != null) {
        String path = "";
        Map params = new LinkedHashMap<>();
        Form queries = getQuery();
        if(queries != null) {
          queries.forEach(query -> {
            params.put(query.getName(), query.getValue());
          });
        }
        long startTime = System.nanoTime();
        CompletableFuture<CustomCodeResponse> future = new CompletableFuture<CustomCodeResponse>();
        CustomCodeResponse futureResult = future.get();
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        long totalTimeMs = TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS);
        LOG.info("CustomCode execution time: " + totalTimeMs + " ms");
        customCodeGet(jarBytes, path, params, is, methodName, 0, future);

        if (futureResult != null) {
          byte[] toStream = null;
          if(futureResult.getResponseBody() != null) {
            toStream = cacheResponse(futureResult.getResponseBody());
          } else {
            toStream = cacheResponse(StringUtil.toByteArray(JSONValue.toJSONString(futureResult.getResponseMap())));
          }
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.setHeader("Content-Length", toStream.length + "");
          response.setHeader("Access-Control-Allow-Origin", "*");
          response.setHeader("Access-Control-Allow-Methods", "*");
          response.setStatus(futureResult.getResponseStatus());
          response.getOutputStream().write(toStream);
          response.getOutputStream().flush();
          response.getOutputStream().close();
        } else {
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.getOutputStream().flush();
          response.getOutputStream().close();
        }
      } else {
        return notFound();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return internalError(stackTraceToString(e));
    }
    return null;
  }

  @Override
  public Representation postMethod(Representation entity) {
//    if (!isAuthorized()) {
//      return unauthorized();
//    }
    LOG.info("CustomCode Name: " + customCodeName);
    LOG.info("CustomCode Method: " + methodName);

    if(getCached()) {
      return cachedRepresentation();
    }

    InputStream jarBytes = getJar(appId, customCodeName);
    if (jarBytes == null) {
      return notFound();
    }
    byte[] content = null;
    InputStream is = null;
    try {
        if(entity != null) {
          if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
            try {
              DiskFileItemFactory factory = new DiskFileItemFactory();
              factory.setSizeThreshold(1000240);
              RestletFileUpload upload = new RestletFileUpload(factory);
              FileItemIterator fileIterator = upload.getItemIterator(entity);
              while (fileIterator.hasNext()) {
                FileItemStream fi = fileIterator.next();
                byte[] bytes = ByteStreams.toByteArray(fi.openStream());
                is = new ByteArrayInputStream(bytes);
              }
            } catch (Exception e) {
              setStatus(Status.SERVER_ERROR_INTERNAL);
              return null;
            }
          } else {
            is = entity.getStream();
          }
        }
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      if (jarBytes != null) {
        String path = "";
        Map params = new LinkedHashMap<>();
        Form queries = getQuery();
        if(queries != null) {
          queries.forEach(query -> {
            params.put(query.getName(), query.getValue());
          });
        }

        long startTime = System.nanoTime();
        CompletableFuture<CustomCodeResponse> future = new CompletableFuture<CustomCodeResponse>();
        customCodePost(jarBytes, path, params, is, methodName, 0, future);
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        long totalTimeMs = TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS);

        LOG.info("CustomCode execution time: " + totalTimeMs + " ms");
        CustomCodeResponse futureResult = future.get();
        if (futureResult != null) {
          byte[] toStream = null;
          if(futureResult.getResponseBody() != null) {
            toStream = cacheResponse(futureResult.getResponseBody());
          } else {
            toStream = cacheResponse(StringUtil.toByteArray(JSONValue.toJSONString(futureResult.getResponseMap())));
          }
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.setHeader("Content-Length", toStream.length + "");
          response.setHeader("Access-Control-Allow-Origin", "*");
          response.setHeader("Access-Control-Allow-Methods", "*");
          response.setStatus(futureResult.getResponseStatus());
          response.getOutputStream().write(toStream);
          response.getOutputStream().flush();
          response.getOutputStream().close();
        } else {
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.getOutputStream().flush();
          response.getOutputStream().close();
        }
      } else {
        return notFound();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return internalError(stackTraceToString(e));
    }
    return null;
  }

  @Override
  public Representation putMethod(Representation entity) {
    if (!isAuthorized()) {
      return unauthorized();
    }
    LOG.info("CustomCode Name: " + customCodeName);
    LOG.info("CustomCode Method: " + methodName);

    if(getCached()) {
      return cachedRepresentation();
    }

    InputStream jarBytes = getJar(appId, customCodeName);
    if (jarBytes == null) {
      return notFound();
    }
    byte[] content = null;
    InputStream is = null;
    try {
      is = entity != null ? entity.getStream() : null;
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      if (jarBytes != null) {
        String path = "";
        Map params = new LinkedHashMap<>();
        Form queries = getQuery();
        if(queries != null) {
          queries.forEach(query -> {
            params.put(query.getName(), query.getValue());
          });
        }

        long startTime = System.nanoTime();
        CompletableFuture<CustomCodeResponse> future = new CompletableFuture<CustomCodeResponse>();
        customCodePut(jarBytes, path, params, is, methodName, 0, future);
        CustomCodeResponse futureResult = future.get();
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        long totalTimeMs = TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS);
        LOG.info("CustomCode execution time: " + totalTimeMs + " ms");

        if (futureResult != null) {
          byte[] toStream = null;
          if(futureResult.getResponseBody() != null) {
            toStream = cacheResponse(futureResult.getResponseBody());
          } else {
            toStream = cacheResponse(StringUtil.toByteArray(JSONValue.toJSONString(futureResult.getResponseMap())));
          }
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.setHeader("Content-Length", toStream.length + "");
          response.setHeader("Access-Control-Allow-Origin", "*");
          response.setHeader("Access-Control-Allow-Methods", "*");
          response.setStatus(futureResult.getResponseStatus());
          response.getOutputStream().write(toStream);
          response.getOutputStream().flush();
          response.getOutputStream().close();
        } else {
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.getOutputStream().flush();
          response.getOutputStream().close();
        }
      } else {
        return notFound();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return internalError(stackTraceToString(e));
    }
    return null;
  }

  @Override
  public Representation deleteMethod(Representation entity) {
    if (!isAuthorized()) {
      return unauthorized();
    }
    LOG.info("CustomCode Name: " + customCodeName);
    LOG.info("CustomCode Method: " + methodName);

    if(getCached()) {
      return cachedRepresentation();
    }

    InputStream jarBytes = getJar(appId, customCodeName);
    if (jarBytes == null) {
      return notFound();
    }
    byte[] content = null;
    InputStream is = null;
    try {
      is = entity != null ? entity.getStream() : null;
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      if (jarBytes != null) {
        String path = "";
        Map params = new LinkedHashMap<>();
        Form queries = getQuery();
        if(queries != null) {
          queries.forEach(query -> {
            params.put(query.getName(), query.getValue());
          });
        }
        long startTime = System.nanoTime();
        CompletableFuture<CustomCodeResponse> future = new CompletableFuture<CustomCodeResponse>();
        customCodeDelete(jarBytes, path, params, is, methodName, 0, future);
        CustomCodeResponse futureResult = future.get();
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        long totalTimeMs = TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS);
        LOG.info("CustomCode execution time: " + totalTimeMs + " ms");

        if (futureResult != null) {
          byte[] toStream = null;
          if(futureResult.getResponseBody() != null) {
            toStream = cacheResponse(futureResult.getResponseBody());
          } else {
            toStream = cacheResponse(StringUtil.toByteArray(JSONValue.toJSONString(futureResult.getResponseMap())));
          }
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.setHeader("Content-Length", toStream.length + "");
          response.setHeader("Access-Control-Allow-Origin", "*");
          response.setHeader("Access-Control-Allow-Methods", "*");
          response.setStatus(futureResult.getResponseStatus());
          response.getOutputStream().write(toStream);
          response.getOutputStream().flush();
          response.getOutputStream().close();
        } else {
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.getOutputStream().flush();
          response.getOutputStream().close();
        }
      } else {
        return notFound();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return internalError(stackTraceToString(e));
    }
    return null;
  }

  @Override
  public void optionsMethod(Representation entity) {
    getMessageHeaders(getResponse()).add("Access-Control-Allow-Origin", "*");
    getMessageHeaders(getResponse()).add("Access-Control-Allow-Methods", "POST,OPTIONS");
    getMessageHeaders(getResponse()).add("Access-Control-Allow-Headers", "Content-Type");
    getMessageHeaders(getResponse()).add("Access-Control-Allow-Credentials", "true");
    getMessageHeaders(getResponse()).add("Access-Control-Max-Age", "60");
  }

  @SuppressWarnings("unchecked")
  static Series<Header> getMessageHeaders(Message message) {
    ConcurrentMap<String, Object> attrs = message.getAttributes();
    Series<Header> headers = (Series<Header>) attrs.get(HEADERS_KEY);
    if (headers == null) {
      headers = new Series<Header>(Header.class);
      Series<Header> prev = (Series<Header>)
              attrs.putIfAbsent(HEADERS_KEY, headers);
      if (prev != null) { headers = prev; }
    }
    return headers;
  }

  private String buildCacheKey() {
    String cacheKey = customCodeName + ":" + methodName + ":";
    List<String> queryKeys = new LinkedList<String>();
    Form queries = getQuery();
    if(queries != null) {
      queries.forEach(query -> {
        queryKeys.add(query.getName());
      });
    }
    Collections.sort(queryKeys);
    for(String queryKey : queryKeys) {
      cacheKey = cacheKey + queryKey + "=" + getQueryValue(queryKey) + ":";
    }
    LOG.info("Cache Key: " + cacheKey);
    return cacheKey;
  }

  private boolean getCached() {
    if(apiArg != null) {
      JSONObject jsonApiArg = new JSONObject(apiArg);
      boolean getCached = jsonApiArg.getBoolean("cached");
      return getCached && cacheService.isExists(buildCacheKey());
    }
    return false;
  }

  private Representation cachedRepresentation() {
    try {
      byte[] cachedBytes = cacheService.get(buildCacheKey());
      HttpServletResponse response = ServletUtils.getResponse(getResponse());
      response.setHeader("Content-Length", cachedBytes.length + "");
      response.getOutputStream().write(cachedBytes);
      response.getOutputStream().flush();
      response.getOutputStream().close();
    } catch (Exception e) {
      return internalError(stackTraceToString(e));
    }
    return null;
  }

  private byte[] cacheResponse(byte[] response) {
    Map<String,Comparable> customCodeMeta = customCodeRepository.getCustomCodeMeta(appId, namespace, customCodeName);
    if(customCodeMeta.get("isCacheable") != null &&  ((Boolean)customCodeMeta.get("isCacheable"))) {
      LOG.info("Caching " + buildCacheKey());
      cacheService.put(buildCacheKey(), response);
    }
    return response;
  }

}
