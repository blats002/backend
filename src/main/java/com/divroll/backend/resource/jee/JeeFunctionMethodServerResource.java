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

import com.alibaba.fastjson.JSON;
import com.divroll.backend.functions.MethodVerb;
import com.divroll.backend.functions.rest.CustomCodeRequest;
import com.divroll.backend.repository.FunctionRepository;
import com.divroll.backend.resource.FunctionMethodResource;
import com.divroll.backend.util.StringUtil;
import com.divroll.functions.CustomCode;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteStreams;
import org.json.simple.JSONValue;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeFunctionMethodServerResource extends BaseServerResource
    implements FunctionMethodResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeApplicationServerResource.class);

    @Inject
    FunctionRepository functionRepository;

    String functionName;
    String methodName;


    @Override
    protected void doInit() {
        super.doInit();
        functionName = getAttribute("functionName");
        methodName = getAttribute("methodName");
    }

    private byte[] getJar(String appId, String functionName) {
        byte[] jarBytes = functionRepository.retrieveFunctionEntity(appId, namespace, functionName);
        return  jarBytes;
    }

    private void customCodeGet(byte[] jarBytes, String path, Map<String,String> params, byte[] body, String methodName, int counter, CompletableFuture<Map<String, ?>> future) {
        CustomCodeRequest request = new CustomCodeRequest
                (MethodVerb.GET, path, params, body, methodName, counter);
        CustomCode customCode = new CustomCode(jarBytes, future);
        customCode.executeMainClass(request);
    }

    private void customCodePost(byte[] jarBytes, String path, Map<String,String> params, byte[] body, String methodName, int counter, CompletableFuture<Map<String, ?>> future) {
        CustomCodeRequest request = new CustomCodeRequest
                (MethodVerb.POST, path, params, body, methodName, counter);
        CustomCode customCode = new CustomCode(jarBytes, future);
        customCode.executeMainClass(request);
    }

    private void customCodePut(byte[] jarBytes, String path, Map<String,String> params, byte[] body, String methodName, int counter, CompletableFuture<Map<String, ?>> future) {
        CustomCodeRequest request = new CustomCodeRequest
                (MethodVerb.PUT, path, params, body, methodName, counter);
        CustomCode customCode = new CustomCode(jarBytes, future);
        customCode.executeMainClass(request);
    }

    private void customCodeDelete(byte[] jarBytes, String path, Map<String,String> params, byte[] body, String methodName, int counter, CompletableFuture<Map<String, ?>> future) {
        CustomCodeRequest request = new CustomCodeRequest
                (MethodVerb.DELETE, path, params, body, methodName, counter);
        CustomCode customCode = new CustomCode(jarBytes, future);
        customCode.executeMainClass(request);
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
        if(!isAuthorized()) {
            return unauthorized();
        }
        LOG.info("Function Name: " + functionName);
        LOG.info("Function Method: " + methodName);
        byte[] jarBytes = getJar(appId, functionName);
        if(jarBytes == null) {
            return notFound();
        }
        byte[] content = null;
        try {
            InputStream is = entity.getStream();
            content = ByteStreams.toByteArray(is);
        } catch (Exception e) {
        }
        try {
            if(jarBytes != null) {
                String path = "";
                CompletableFuture<Map<String,?>> future = new CompletableFuture<Map<String,?>>();
                customCodeGet(jarBytes, path, new LinkedHashMap<>(), content, methodName, 0, future);
                Map<String,?> futureResult = future.get();
                if(futureResult != null) {
                    byte[] toStream = StringUtil.toByteArray(JSONValue.toJSONString(futureResult));
                    HttpServletResponse response = ServletUtils.getResponse(getResponse());
                    response.setHeader("Content-Length", toStream.length + "");
                    response.setHeader("Access-Control-Allow-Origin", "*");
                    response.getOutputStream().write(toStream);
                    response.getOutputStream().flush();
                    response.getOutputStream().close();
                }
                LOG.info("Custom Code Response: " + JSON.toJSONString(futureResult));
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
        if(!isAuthorized()) {
            return unauthorized();
        }
        LOG.info("Function Name: " + functionName);
        LOG.info("Function Method: " + methodName);
        byte[] jarBytes = getJar(appId, functionName);
        if(jarBytes == null) {
            return notFound();
        }
        byte[] content = null;
        try {
            InputStream is = entity.getStream();
            content = ByteStreams.toByteArray(is);
        } catch (Exception e) {
        }
        try {
            if(jarBytes != null) {
                String path = "";
                CompletableFuture<Map<String,?>> future = new CompletableFuture<Map<String,?>>();
                customCodePost(jarBytes, path, new LinkedHashMap<>(), content, methodName, 0, future);
                Map<String,?> futureResult = future.get();
                if(futureResult != null) {
                    byte[] toStream = StringUtil.toByteArray(JSONValue.toJSONString(futureResult));
                    HttpServletResponse response = ServletUtils.getResponse(getResponse());
                    response.setHeader("Content-Length", toStream.length + "");
                    response.setHeader("Access-Control-Allow-Origin", "*");
                    response.getOutputStream().write(toStream);
                    response.getOutputStream().flush();
                    response.getOutputStream().close();
                }
                LOG.info("Custom Code Response: " + JSON.toJSONString(futureResult));
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
        if(!isAuthorized()) {
            return unauthorized();
        }
        LOG.info("Function Name: " + functionName);
        LOG.info("Function Method: " + methodName);
        byte[] jarBytes = getJar(appId, functionName);
        if(jarBytes == null) {
            return notFound();
        }
        byte[] content = null;
        try {
            InputStream is = entity.getStream();
            content = ByteStreams.toByteArray(is);
        } catch (Exception e) {
        }
        try {
            if(jarBytes != null) {
                String path = "";
                CompletableFuture<Map<String,?>> future = new CompletableFuture<Map<String,?>>();
                customCodePut(jarBytes, path, new LinkedHashMap<>(), content, methodName, 0, future);
                Map<String,?> futureResult = future.get();
                if(futureResult != null) {
                    byte[] toStream = StringUtil.toByteArray(JSONValue.toJSONString(futureResult));
                    HttpServletResponse response = ServletUtils.getResponse(getResponse());
                    response.setHeader("Content-Length", toStream.length + "");
                    response.setHeader("Access-Control-Allow-Origin", "*");
                    response.getOutputStream().write(toStream);
                    response.getOutputStream().flush();
                    response.getOutputStream().close();
                }
                LOG.info("Custom Code Response: " + JSON.toJSONString(futureResult));
            } else {
                return notFound();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalError(stackTraceToString(e));
        }
        return null;    }

    @Override
    public Representation deleteMethod(Representation entity) {
        if(!isAuthorized()) {
            return unauthorized();
        }
        LOG.info("Function Name: " + functionName);
        LOG.info("Function Method: " + methodName);
        byte[] jarBytes = getJar(appId, functionName);
        if(jarBytes == null) {
            return notFound();
        }
        byte[] content = null;
        try {
            InputStream is = entity.getStream();
            content = ByteStreams.toByteArray(is);
        } catch (Exception e) {
        }
        try {
            if(jarBytes != null) {
                String path = "";
                CompletableFuture<Map<String,?>> future = new CompletableFuture<Map<String,?>>();
                customCodeDelete(jarBytes, path, new LinkedHashMap<>(), content, methodName, 0, future);
                Map<String,?> futureResult = future.get();
                if(futureResult != null) {
                    byte[] toStream = StringUtil.toByteArray(JSONValue.toJSONString(futureResult));
                    HttpServletResponse response = ServletUtils.getResponse(getResponse());
                    response.setHeader("Content-Length", toStream.length + "");
                    response.setHeader("Access-Control-Allow-Origin", "*");
                    response.getOutputStream().write(toStream);
                    response.getOutputStream().flush();
                    response.getOutputStream().close();
                }
                LOG.info("Custom Code Response: " + JSON.toJSONString(futureResult));
            } else {
                return notFound();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalError(stackTraceToString(e));
        }
        return null;
    }
}
