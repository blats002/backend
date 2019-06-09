/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019, Divroll, and individual contributors
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

import com.divroll.backend.customcode.MethodVerb;
import com.divroll.backend.customcode.rest.CustomCodeRequest;
import com.google.common.io.ByteStreams;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class TestCustomCode {

  static String JAR_FILE =
      "../customcode-sdk-java-example/target/customcode-sdk-java-example-0-SNAPSHOT.jar";
  static int API_VERSION = 1;
  static String USER = "adam";
  private static Logger LOGGER = Logger.getLogger(TestCustomCode.class.getName());

  @Before
  public void setUp() {}

  @After
  public void tearDown() {}

  @Test
  public void doTest() {}

  @Test
  public void testExecuteMainClass() throws Exception {
    Map<String, String> params = new LinkedHashMap<String, String>();
    CustomCodeRequest request =
        new CustomCodeRequest(
            MethodVerb.GET, "/test", params, new ByteArrayInputStream("world".getBytes()), "hello_method", 0L);
    File file = new File(JAR_FILE);
    CompletableFuture<Map<String, ?>> future = new CompletableFuture<Map<String, ?>>();
    CustomCode customCode = new CustomCode(ByteStreams.toByteArray(new FileInputStream(file)), future);
    customCode.executeMainClass(request);
    Map<String, ?> result = future.get();
    System.out.println(JSONValue.toJSONString(future));
  }

  private byte[] fileToBytes(File file) {
    byte[] b = new byte[(int) file.length()];
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      fileInputStream.read(b);
    } catch (FileNotFoundException e) {
      LOGGER.info("File Not Found.");
      e.printStackTrace();
    } catch (IOException e1) {
      LOGGER.info("Error Reading The File.");
      e1.printStackTrace();
    }
    return b;
  }
}
