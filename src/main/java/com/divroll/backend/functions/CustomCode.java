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
package com.divroll.backend.functions;

import com.divroll.backend.customcode.method.CustomCodeMethod;
import com.divroll.backend.customcode.jar.JarEntryObject;
import com.divroll.backend.customcode.rest.CustomCodeRequest;
import com.google.common.io.ByteStreams;
import com.google.common.reflect.ClassPath;
import kotlin.Pair;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.TeeInputStream;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class CustomCode {

  private static Logger LOGGER = Logger.getLogger(CustomCode.class.getName());
  private static String MAIN_CLASS = "Main-Class";
  // private CustomCodeEventListener listener;
  private CompletableFuture<Map<String, ?>> future;
  private InputStream jar;

  public CustomCode() {}

  //	public CustomCode(byte[] jar, CustomCodeEventListener listener) {
  //		this.listener = listener;
  //		this.jar = jar;
  //	}

  public CustomCode(InputStream jar, CompletableFuture<Map<String, ?>> future) {
    this.jar = jar;
    this.future = future;
  }

  /**
   * Execute the main class defined in the {@code pom.xml}
   *
   * @param request
   */
  public void executeMainClass(CustomCodeRequest request) {
    LOGGER.info("Execute main class");
    String classToLoad = null;
    String methodName = request.getMethodName();

    final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();



    try{

      byte[] bytes = ByteStreams.toByteArray(jar);

      Pair<String,JarInputStream> pair = extractMainClassManifest(new ByteArrayInputStream(bytes));
      classToLoad = pair.getFirst();
      LOGGER.info("Class to load: " + classToLoad);
      //JarByteClassloader loader = new JarByteClassloader(pair.getSecond());
      JarByteClassloader loader = new JarByteClassloader(new JarInputStream(new ByteArrayInputStream(bytes)));

      ClassPath cp= ClassPath.from(loader);
      for(ClassPath.ClassInfo info : cp.getAllClasses()) {
        if(info.getName().contains("divroll")) {
          System.out.println(info.getName());
        }
        if(info.getName().contains("")) {
          System.out.println(info.getName());
        }
      }

      Class c = loader.loadClass(classToLoad);
      Thread.currentThread().setContextClassLoader(loader);
      JarEntryObject jarEntry = (JarEntryObject) c.newInstance();
      List<CustomCodeMethod> methods = jarEntry.methods();
      for (CustomCodeMethod cc : methods) {
        String ccMethodName = cc.getMethodName();
        if (methodName.equals(ccMethodName)) {
          // TODO: Throws NPE when CustomCodeResponse is null
          Map<String, ?> result = cc.execute(request).getResponseMap();
          if (result != null) {
            // listener.onSuccess(result); // TODO: remove this
            future.complete(result);
          }
          LOGGER.info("Result: " + JSONValue.toJSONString(result));
        }
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } finally{
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    /*
    try {
    } catch (ClassNotFoundException e) {
      // listener.onFailure(new EntryPointClassNotFound(classToLoad));
      future.completeExceptionally(e);
      e.printStackTrace();
    } catch (InstantiationException e) {
      // listener.onFailure(new CustomCodeException(e.getMessage()));
      future.completeExceptionally(e);
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // listener.onFailure(new CustomCodeException(e.getMessage()));
      future.completeExceptionally(e);
      e.printStackTrace();
    } catch (Exception e) {
      // listener.onFailure(new CustomCodeException(e.getMessage()));
      future.completeExceptionally(e);
      e.printStackTrace();
    }*/
  }

  /**
   * Extracts the POM XML string from a jar bytes
   *
   * @param inputStream
   * @return the String represented pom.xml
   */
  private Pair<String,JarInputStream> extractMainClassManifest(InputStream inputStream) {
    String mainClass = null;
    JarInputStream jis = null;
    byte[] bytes = null;
    try {
      bytes = ByteStreams.toByteArray(inputStream);
      jis = new JarInputStream(new ByteArrayInputStream(bytes));
      final Manifest manifest = jis.getManifest();
      final Attributes mattr = manifest.getMainAttributes();
      for (Object a : mattr.keySet()) {
        String attr = String.valueOf(a);
        if (attr.equals(MAIN_CLASS)) {
          mainClass = mattr.getValue((Name) a);
          LOGGER.info(attr + " : " + mainClass);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      return new Pair<String,JarInputStream>(mainClass, new JarInputStream(new ByteArrayInputStream(bytes)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private byte[] getJar(String subdomain, String functionName) {
    byte[] jarBytes = null;
//    try {
//      // TODO: Use masterKey
//      Parse.initialize(parseAppId, parseRestApiKey, getParseUrl());
//      ParseQuery<ParseObject> query = ParseQuery.getQuery("CustomCode");
//      query.whereEqualTo("function", functionName)
//              .whereEqualTo("appId", subdomain);
//      List<ParseObject> results = query.find();
//      ParseObject result = results.iterator().next();
//      if(result != null) {
//        ParseFile parseFile = result.getParseFile("jar");
//        LOG.info("Filename: " + parseFile.getName());
//        LOG.info("File URL: " + parseFile.getUrl());
//        String url = parseFile.getUrl().replace("https://", "http://");
//        parseFile.setUrl(url);
//        LOG.info("Final File URL: " + parseFile.getUrl());
//        jarBytes = parseFile.getData();
//        LOG.info("Function: " + result.getString("function"));
//        LOG.info("Jar size: " + jarBytes.length);
//        //cacheService.put(subdomain + KEY_SPACE + functionName, jarBytes);
//        return jarBytes;
//      } else {
//        LOG.info("Null parse object");
//      }
//      //
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
    return null;
  }


}
