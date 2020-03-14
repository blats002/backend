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
package com.divroll.backend.service.jee;

import com.divroll.backend.customcode.MethodVerb;
import com.divroll.backend.customcode.rest.CustomCodeRequest;
import com.divroll.backend.customcode.rest.CustomCodeResponse;
import com.divroll.backend.customcodes.CustomCode;
import com.divroll.backend.service.CustomCodeService;
import com.divroll.backend.util.StringUtil;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteStreams;
import jetbrains.exodus.core.dataStructures.Pair;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeCustomCodeService implements CustomCodeService {
  private static final Logger LOG = LoggerFactory.getLogger(JeeCustomCodeService.class);

  private static final int DEFAULT_TIMEOUT = 60;
  private int customCodeTimeout = DEFAULT_TIMEOUT;

  @Override
  public Pair<byte[], Exception> customCodePost(
          InputStream jarBytes,
          String path,
          Map<String, String> params,
          InputStream body,
          String methodName) {
    LOG.info("Started customCodePost method");
    byte[] byteResponse = null;
    try {
      int counter = 0;
      CompletableFuture<CustomCodeResponse> future = new CompletableFuture<CustomCodeResponse>();
      String CUSTOM_CODE_TIMEOUT = System.getenv("CUSTOM_CODE_TIMEOUT");
      if(CUSTOM_CODE_TIMEOUT != null && !CUSTOM_CODE_TIMEOUT.isEmpty()) {
        customCodeTimeout = Integer.valueOf(CUSTOM_CODE_TIMEOUT);
      }
      CustomCodeRequest request =
              new CustomCodeRequest(MethodVerb.POST, path, params, body, methodName, counter);
      CustomCode customCode = new CustomCode(ByteStreams.toByteArray(jarBytes), future);
      customCode.executeMainClass(request, customCodeTimeout);
      CustomCodeResponse futureResult = future.get();
      byteResponse = StringUtil.toByteArray(JSONValue.toJSONString(futureResult.getResponseMap()));
    } catch (Exception e) {
      LOG.info("Finished customCodePost method with error: " + e.getMessage());
      return new Pair<>(byteResponse, e);
    }
    LOG.info("Finished customCodePost method");
    return new Pair<>(byteResponse, null);
  }

}
