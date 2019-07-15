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
 */
package com.divroll.backend.resource.jee;

import com.alibaba.fastjson.JSON;
import com.divroll.backend.customcode.MethodVerb;
import com.divroll.backend.customcode.rest.CustomCodeRequest;
import com.divroll.backend.customcodes.CustomCode;
import com.divroll.backend.repository.CustomCodeRepository;
import com.divroll.backend.resource.CustomCodeMethodResource;
import com.divroll.backend.util.StringUtil;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteStreams;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
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
import java.util.LinkedHashMap;
import java.util.Map;
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
  private static final int DEFAULT_TIMEOUT = 10;

  @Inject
  CustomCodeRepository customCodeRepository;

  String customCodeName;
  String methodName;

  @Override
  protected void doInit() {
    super.doInit();
    customCodeName = getAttribute("customCodeName");
    methodName = getAttribute("methodName");
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
      CompletableFuture<Map<String, ?>> future) throws IOException {
    CustomCodeRequest request =
        new CustomCodeRequest(MethodVerb.GET, path, params, body, methodName, counter);
    CustomCode customCode = new CustomCode(ByteStreams.toByteArray(jarBytes), future);
    customCode.executeMainClass(request, DEFAULT_TIMEOUT);
  }

  private void customCodePost(
          InputStream jarBytes,
      String path,
      Map<String, String> params,
          InputStream body,
      String methodName,
      int counter,
      CompletableFuture<Map<String, ?>> future) throws IOException {
    CustomCodeRequest request =
        new CustomCodeRequest(MethodVerb.POST, path, params, body, methodName, counter);
    CustomCode customCode = new CustomCode(ByteStreams.toByteArray(jarBytes), future);
    customCode.executeMainClass(request, DEFAULT_TIMEOUT);
  }

  private void customCodePut(
          InputStream jarBytes,
      String path,
      Map<String, String> params,
          InputStream body,
      String methodName,
      int counter,
      CompletableFuture<Map<String, ?>> future) throws IOException {
    CustomCodeRequest request =
        new CustomCodeRequest(MethodVerb.PUT, path, params, body, methodName, counter);
    CustomCode customCode = new CustomCode(ByteStreams.toByteArray(jarBytes), future);
    customCode.executeMainClass(request, DEFAULT_TIMEOUT);
  }

  private void customCodeDelete(
          InputStream jarBytes,
      String path,
      Map<String, String> params,
          InputStream body,
      String methodName,
      int counter,
      CompletableFuture<Map<String, ?>> future) throws IOException {
    CustomCodeRequest request =
        new CustomCodeRequest(MethodVerb.DELETE, path, params, body, methodName, counter);
    CustomCode customCode = new CustomCode(ByteStreams.toByteArray(jarBytes), future);
    customCode.executeMainClass(request, DEFAULT_TIMEOUT);
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
    InputStream jarBytes = getJar(appId, customCodeName);
    if (jarBytes == null) {
      return notFound();
    }
    byte[] content = null;
    InputStream is = null;
    try {
      is = entity.getStream();
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
        CompletableFuture<Map<String, ?>> future = new CompletableFuture<Map<String, ?>>();
        Map<String, ?> futureResult = future.get();
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        long totalTimeMs = TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS);
        LOG.info("CustomCode execution time: " + totalTimeMs + " ms");
        customCodeGet(jarBytes, path, params, is, methodName, 0, future);

        if (futureResult != null) {
          byte[] toStream = StringUtil.toByteArray(JSONValue.toJSONString(futureResult));
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.setHeader("Content-Length", toStream.length + "");
          response.getOutputStream().write(toStream);
          response.getOutputStream().flush();
          response.getOutputStream().close();
        } else {
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.getOutputStream().flush();
          response.getOutputStream().close();
        }
        LOG.info("CustomCode Response: " + JSON.toJSONString(futureResult));
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
    InputStream jarBytes = getJar(appId, customCodeName);
    if (jarBytes == null) {
      return notFound();
    }
    byte[] content = null;
    InputStream is = null;
    try {
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
        CompletableFuture<Map<String, ?>> future = new CompletableFuture<Map<String, ?>>();
        customCodePost(jarBytes, path, params, is, methodName, 0, future);
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        long totalTimeMs = TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS);

        LOG.info("CustomCode execution time: " + totalTimeMs + " ms");
        Map<String, ?> futureResult = future.get();
        if (futureResult != null) {
          byte[] toStream = StringUtil.toByteArray(JSONValue.toJSONString(futureResult));
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.setHeader("Content-Length", toStream.length + "");
          response.getOutputStream().write(toStream);
          response.getOutputStream().flush();
          response.getOutputStream().close();
        } else {
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.getOutputStream().flush();
          response.getOutputStream().close();
        }
        LOG.info("CustomCode Response: " + JSON.toJSONString(futureResult));
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
    InputStream jarBytes = getJar(appId, customCodeName);
    if (jarBytes == null) {
      return notFound();
    }
    byte[] content = null;
    InputStream is = null;
    try {
      is = entity.getStream();
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
        CompletableFuture<Map<String, ?>> future = new CompletableFuture<Map<String, ?>>();
        customCodePut(jarBytes, path, params, is, methodName, 0, future);
        Map<String, ?> futureResult = future.get();
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        long totalTimeMs = TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS);
        LOG.info("CustomCode execution time: " + totalTimeMs + " ms");

        if (futureResult != null) {
          byte[] toStream = StringUtil.toByteArray(JSONValue.toJSONString(futureResult));
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.setHeader("Content-Length", toStream.length + "");
          response.getOutputStream().write(toStream);
          response.getOutputStream().flush();
          response.getOutputStream().close();
        } else {
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.getOutputStream().flush();
          response.getOutputStream().close();
        }
        LOG.info("CustomCode Response: " + JSON.toJSONString(futureResult));
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
    InputStream jarBytes = getJar(appId, customCodeName);
    if (jarBytes == null) {
      return notFound();
    }
    byte[] content = null;
    InputStream is = null;
    try {
      is = entity.getStream();
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
        CompletableFuture<Map<String, ?>> future = new CompletableFuture<Map<String, ?>>();
        customCodeDelete(jarBytes, path, params, is, methodName, 0, future);
        Map<String, ?> futureResult = future.get();
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        long totalTimeMs = TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS);
        LOG.info("CustomCode execution time: " + totalTimeMs + " ms");

        if (futureResult != null) {
          byte[] toStream = StringUtil.toByteArray(JSONValue.toJSONString(futureResult));
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.setHeader("Content-Length", toStream.length + "");
          response.getOutputStream().write(toStream);
          response.getOutputStream().flush();
          response.getOutputStream().close();
        } else {
          HttpServletResponse response = ServletUtils.getResponse(getResponse());
          response.getOutputStream().flush();
          response.getOutputStream().close();
        }
        LOG.info("CustomCode Response: " + JSON.toJSONString(futureResult));
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

}
