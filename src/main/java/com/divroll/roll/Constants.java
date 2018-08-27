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
package com.divroll.roll;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public final class Constants {
    public static final String SERVER_NAME = "Divroll";
    public static final String HEADER_MASTER_KEY = "X-Divroll-Master-Key";
    public static final String HEADER_API_KEY = "X-Divroll-Api-Key";
    public static final String HEADER_APP_ID = "X-Divroll-App-Id";
    public static final String HEADER_AUTH_TOKEN = "X-Divroll-Auth-Token";
    public static final String HEADER_ACL_READ = "X-Divroll-ACL-Read";
    public static final String HEADER_ACL_WRITE = "X-Divroll-ACL-Write";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String MASTER_KEY = "masterKey";
    public static final String API_KEY = "apiKey";
    public static final String APP_ID = "appId";
    public static final String APP_NAME = "appName";
    public static final String ENTITY_ID = "entityId";
    public static final String ENTITY_TYPE = "entityType";
    public static final String USER_ID = "userId";
    public static final String ROLE_ID = "roleId";
    public static final String ROLE_NAME = "name";
    public static final String ROLE_LINKNAME = "role"; // TODO add to app.properties file instead
    public static final String WEBTOKEN = "webToken";
    public static final String QUERY_USERNAME = "username";
    public static final String QUERY_PASSWORD = "password";
    public static final String QUERY_SKIP = "skip";
    public static final String QUERY_LIMIT = "limit";
    public static final String JWT_ID_KEY = "id";
    public static final String DEFAULT_CHARSET = "utf-8";
    public static final String ACL_ASTERISK = "*";
    public static final String ENTITYSTORE_APPLICATION = "Application";
    //public static final String METADATA_KEY = "_md";
    public static final String BLOBNAMES = "blobnames";
    public static final String LINKS = "links";
    public static final String ACL_READ = "aclRead";
    public static final String ACL_WRITE = "aclWrite";
    public static final String RESERVED_FIELD_PUBLICWRITE = "publicWrite";
    public static final String RESERVED_FIELD_PUBLICREAD = "publicRead";
    public static final String RESERVED_FIELD_USERNAME = "username";
    public static final String RESERVED_FIELD_PASSWORD = "password";
    public static final String ERROR_MISSING_AUTH_TOKEN = "Missing auth token";
    public static final String ERROR_INVALID_AUTH_TOKEN = "Invalid auth token";
    public static final String ERROR_INVALID_ACL = "Invalid ACL";
    public static final String ERROR_MISSING_USERNAME_PASSWORD = "Missing username/password pair";
    public static final String ERROR_USERNAME_EXISTS = "Username already exists";
    public static final String ERROR_MISSING_ROLE_ID = "Missing Role ID in request";
    public static final String ERROR_MISSING_USER_ID = "Missing User ID in path";
    public static final String ERROR_ENTITY_WAS_REMOVED = "Entity was removed";
    public static final String ERROR_APPLICATION_NOT_FOUND = "Application not found";
    public static final String ERROR_CANNOT_DELETE_USER = "Cannot delete user or user does not exist";
    public static final String ERROR_QUERY_USERNAME_REQUIRED = "Query parameter username is required";
    public static final String ERROR_MASTERKEY_INVALID = "Invalid Application ID and/or Master Key";
    public static final String ERROR_MASTERKEY_MISSING = "Missing Application ID or Master Key";

    private Constants() {
    }
}
