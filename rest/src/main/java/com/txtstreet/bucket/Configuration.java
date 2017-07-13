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
    public static final String TXTSTREET_PARSE_URL = "https://parse2.divroll.space/parse";
//    public static final String TXTSTREET_PARSE_URL = "***REMOVED***";
//    public static final String TXTSTREET_PARSE_URL = "https://parse.divroll.space/parse";
//    public static final String TXTSTREET_PARSE_URL = "***REMOVED***";
//    public static final String TXTSTREET_PARSE_URL = "http://10.88.17.40/parse";
//    public static final String TXTSTREET_PARSE_URL = "http://localhost:1337/parse";
    public static final String TXTSTREET_PARSE_APP_ID = "";
    public static final String TXTSTREET_PARSE_REST_API_KEY = "***REMOVED***";
    public static final String TXTSTREET_MASTER_KEY = "";
    public static final String ME_URI = "/users/me";
    public static final String LOGIN_URI = "/login?";
    public static final String CONFIG_URI = "/config";

    // Jelastic
    public final static String JELASTIC_PLATFORM_APPID = "1dd8d191d38fff45e62564fcf67fdcd6";
    //public final static String JELASTIC_DASHBOARD_APPID = "77047754c838ee6badea32b5afab1882";
    public final static String JELASTIC_CLUSTER_APPID = "58bdf83fea6af021e0c94ba13730fd6b";
    public final static String JELASTIC_HOSTER_URL = "https://app.divroll.space/1.0/";
    public final static String JELASTIC_AUTH_HOSTER_URL = "https://app.divroll.space/1.0/users/authentication/";
    public final static String JELASTIC_ENV_HOSTER_URL = "https://app.divroll.space/1.0/environment/";
    public final static String JELASTIC_USER_EMAIL = "webmaster@divroll.com";
    public final static String JELASTIC_USER_PASSWORD = "2bESNZpw04";
    public final static String JELASTIC_ENV_NAME = "roller";
//    public final static Integer JELASTIC_NGINX_NODE_ID = 2041; // roller.divroll.space Nginx
    public final static Integer JELASTIC_NGINX_NODE_ID = 2476; // roller-td.divrll.space Nginx
}
