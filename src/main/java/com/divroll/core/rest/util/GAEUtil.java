/*
*
* Copyright (c) 2016 Kerby Martino and Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   https://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/
package com.divroll.core.rest.util;

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
