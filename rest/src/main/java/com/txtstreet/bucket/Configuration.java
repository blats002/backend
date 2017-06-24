/*
*
* Copyright (c) 2017 Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/
package com..bucket;

/**
 * Application configuration
 */
public class Configuration {
    // Parse
    public static final String TXTSTREET_PARSE_URL = "***REMOVED***";
    //    public static final String TXTSTREET_PARSE_URL = "http://10.88.17.16/parse";
    public static final String TXTSTREET_PARSE_APP_ID = "";
    public static final String TXTSTREET_PARSE_REST_API_KEY = "***REMOVED***";
    public static final String TXTSTREET_MASTER_KEY = "";
    public static final String ME_URI = "/users/me";
    public static final String LOGIN_URI = "/login?";
    public static final String CONFIG_URI = "/config";

    // Jelastic
    public final static String JELASTIC_PLATFORM_APPID = "";
    //public final static String JELASTIC_DASHBOARD_APPID = "";
    public final static String JELASTIC_CLUSTER_APPID = "";
    public final static String JELASTIC_HOSTER_URL = "";
    public final static String JELASTIC_AUTH_HOSTER_URL = "";
    public final static String JELASTIC_ENV_HOSTER_URL = "";
    public final static String JELASTIC_USER_EMAIL = "";
    public final static String JELASTIC_USER_PASSWORD = "";
    public final static String JELASTIC_ENV_NAME = "";
    public final static Integer JELASTIC_NGINX_NODE_ID = 2041;
}
