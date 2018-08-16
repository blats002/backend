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
package com.divroll.domino.resource.jee;

import com.alibaba.fastjson.JSONObject;
import com.divroll.domino.Constants;
import com.divroll.domino.guice.SelfInjectingServerResource;
import com.divroll.domino.model.Application;
import com.divroll.domino.service.ApplicationService;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class BaseServerResource extends SelfInjectingServerResource {

    private static final Logger LOG
            = Logger.getLogger(BaseServerResource.class.getName());

    protected Map<String, Object> queryMap = new LinkedHashMap<>();
    protected Map<String, String> propsMap = new LinkedHashMap<>();
    protected String entityId;
    protected String entityType;

    protected String appId;
    protected String apiKey;
    protected String masterKey;
    protected String authToken;

    protected String aclRead;
    protected String aclWrite;

    protected String accept;
    protected String contentType;

    protected String userId;
    protected String username;

    protected String roleId;

    @Inject
    ApplicationService applicationService;

    @Override
    protected void doInit() {
        super.doInit();
        getHeaders();
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series(Header.class);
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }
        responseHeaders.add(new Header("X-Powered-By", Constants.SERVER_NAME));
        setAllowedMethods(Sets.newHashSet(Method.GET,
                Method.PUT,
                Method.POST,
                Method.DELETE,
                Method.OPTIONS));
        propsMap = appProperties();
        entityId = getAttribute(Constants.ENTITY_ID);
        entityType = getAttribute(Constants.ENTITY_TYPE);
        userId = getAttribute(Constants.USER_ID);
        roleId = getAttribute(Constants.ROLE_ID);

        username = getQueryValue(Constants.QUERY_USERNAME);

        Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
        appId = headers.getFirstValue(Constants.HEADER_APP_ID);
        apiKey = headers.getFirstValue(Constants.HEADER_API_KEY);
        masterKey = headers.getFirstValue(Constants.HEADER_MASTER_KEY);
        authToken = headers.getFirstValue(Constants.HEADER_AUTH_TOKEN);

        aclRead = headers.getFirstValue(Constants.HEADER_ACL_READ);
        aclWrite = headers.getFirstValue(Constants.HEADER_ACL_WRITE);

        accept = headers.getFirstValue(Constants.HEADER_ACCEPT);
        contentType = headers.getFirstValue(Constants.HEADER_CONTENT_TYPE);

    }

    protected void getHeaders() {
        Series<Header> headers = (Series<Header>) getRequestAttributes().get("org.restlet.http.headers");
    }

    protected Map<String, String> appProperties() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        InputStream is = getContext().getClass().getResourceAsStream("/app.properties");
        Properties props = new Properties();
        try {
            props.load(is);
            map = new LinkedHashMap<String, String>((Map) props);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    protected byte[] toByteArray(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    protected Object fromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }

    protected boolean isAuthorized(String appId, String apiKey, String masterKey) {
        if (appId != null) {
            Application app = applicationService.read(appId);
            if (app != null) {
                if (BCrypt.checkpw(masterKey, app.getMasterKey())) {
                    return true;
                }
                if (BCrypt.checkpw(apiKey, app.getApiKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isMaster(String appId, String masterKey) {
        if (appId != null) {
            Application app = applicationService.read(appId);
            if (app != null) {
                if (BCrypt.checkpw(masterKey, app.getMasterKey())) {
                    return true;
                }
            }
        }
        return false;
    }


    protected Representation returnNull() {
        JSONObject jsonObject = new JSONObject();
        return new JsonRepresentation(jsonObject.toJSONString());
    }

    protected Representation returnServerError() {
        JSONObject jsonObject = new JSONObject();
        setStatus(Status.SERVER_ERROR_INTERNAL);
        return new JsonRepresentation(jsonObject.toJSONString());
    }

    protected Representation returnMissingAuthToken() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", Constants.ERROR_MISSING_AUTH_TOKEN);
        return new JsonRepresentation(jsonObject.toJSONString());
    }

    protected Representation missingUsernamePasswordPair() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", Constants.ERROR_MISSING_USERNAME_PASSWORD);
        return new JsonRepresentation(jsonObject.toJSONString());
    }

    protected Map<String, Object> cleanup(Map<String,Object> result) {
        result.remove(Constants.RESERVED_FIELD_PUBLICWRITE);
        result.remove(Constants.RESERVED_FIELD_PUBLICREAD);
        return result;
    }

}
