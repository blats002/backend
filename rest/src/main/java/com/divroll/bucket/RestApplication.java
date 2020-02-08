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
package com.divroll.bucket;

import com.divroll.bucket.resource.CalculateFileSizeResource;
import com.divroll.bucket.resource.jee.*;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.engine.application.CorsFilter;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.util.Arrays;
import java.util.HashSet;

public class RestApplication extends Application {
  final static Logger LOG
          = LoggerFactory.getLogger(RestApplication.class);
  private static final String ROOT_URI = "/";
  /**
   * Creates a root Restlet that will receive all incoming calls.
   */
  @Override
  public Restlet createInboundRoot() {

    Router router = new Router(getContext());

    Directory directory = new Directory(getContext(), "war:///doc");
    directory.setIndexName("index.html");
    router.attach(ROOT_URI + "websites/{subdomain}", WebsiteUploadServerResource.class);
    router.attach(ROOT_URI + "websites/{subdomain}/custom_ssls/{domain}", SSLServerResource.class);
    router.attach(ROOT_URI + "files", CalculateFileSizeServerResource.class);
    router.attach(ROOT_URI + "migrate", CopyFromParseToGoogleCloudServerResource.class);
    router.attachDefault(directory);

    CorsFilter corsFilter = new CorsFilter(getContext());
    corsFilter.setAllowedHeaders(new HashSet<String>(Arrays.asList("Content-Type")));
    corsFilter.setAllowedOrigins(new HashSet(Arrays.asList("*")));
    corsFilter.setAllowedCredentials(true);
    corsFilter.setAllowingAllRequestedHeaders(true);
    corsFilter.setSkippingResourceForCorsOptions(true);

    corsFilter.setNext(router);

    getConnectorService().getClientProtocols().add(Protocol.HTTPS);
    getConnectorService().getClientProtocols().add(Protocol.HTTP);

    // Create all-trusting host name verifier
    HostnameVerifier allHostsValid = new HostnameVerifier() {
      public boolean verify(String hostname, SSLSession session) {
        return true;
      }
    };
    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    return corsFilter;
  }
}
