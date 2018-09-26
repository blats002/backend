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
package com.divroll.backend.pubsub;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.config.service.Singleton;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.handler.AtmosphereHandlerAdapter;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.atmosphere.util.SimpleBroadcaster;

import java.io.IOException;

@Singleton
@AtmosphereHandlerService(path = "/{topic}",
        interceptors = {
                AtmosphereResourceLifecycleInterceptor.class,
                AuthenticationInterceptor.class,
                TrackMessageSizeInterceptor.class,
                BroadcastOnPostAtmosphereInterceptor.class,
                SuspendTrackerInterceptor.class},
        broadcaster = SimpleBroadcaster.class)
public class AtmosphereHandlerPubSub extends AtmosphereHandlerAdapter {

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        if (event.isSuspended()) {
            String message = event.getMessage() == null ? null : event.getMessage().toString();
            event.getResource().write(message);
        }
    }

}
