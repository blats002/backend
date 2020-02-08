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
package com.divroll.backend.guice;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import org.restlet.ext.servlet.ServerServlet;

import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class GuiceServletModule extends ServletModule {
  private static Map<String, String> map(String... params) {
    Preconditions.checkArgument(
        params.length % 2 == 0, "You have to have a n even number of map params");
    Map<String, String> map = Maps.newHashMap();
    for (int i = 0; i < params.length; i += 2) {
      map.put(params[i], params[i + 1]);
    }
    return map;
  }

  @Override
  protected void configureServlets() {
    bind(ServerServlet.class).in(Scopes.SINGLETON);
  }
}
