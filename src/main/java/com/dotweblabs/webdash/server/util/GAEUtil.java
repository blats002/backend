/**
 *
 * Copyright (c) 2015 Hunchee and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *                 __               __ __
 * .--.--.--------|  |--.----.-----|  |  .---.-.
 * |  |  |        |  _  |   _|  -__|  |  |  _  |
 * |_____|__|__|__|_____|__| |_____|__|__|___._|
 * :: Commons, GWT and GAE Utility classes    ::
 *
 */
package com.divroll.webdash.server.util;

import com.google.appengine.api.utils.SystemProperty;

public final class GAEUtil {

    private GAEUtil() {
    }

    /**
     * Indicates if current JVM is running on Google App Engine.
     * @see <a href="http://code.google.com/appengine/docs/java/runtime.html#The_Environment">GAE documentation</a>
     * @return true if running in GAE mode.
     */
    public static boolean isGaeMode() {
        return System.getProperty("com.google.appengine.runtime.environment") != null;
    }
    
    public static boolean isGaeProd() {
    	return (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production);
    }

    public static boolean isGaeDev() {
        return (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development);
    }
}
