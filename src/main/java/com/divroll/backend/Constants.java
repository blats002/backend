/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public final class Constants {

  public static final String APP_DOMAIN = "APP_DOMAIN";
  public static final String DEFAULT_ACTIVATION_BASE_URL = "DEFAULT_ACTIVATION_BASE_URL";
  public static final String DEFAULT_PASSWORD_RESET_BASE_URL = "DEFAULT_PASSWORD_RESET_BASE_URL";

  public static final String SERVER_NAME = "Divroll";
  public static final String HEADER_SUPER_AUTH = "X-Divroll-Super-Auth-Token";
  public static final String HEADER_MASTER_KEY = "X-Divroll-Master-Key";
  public static final String HEADER_API_KEY = "X-Divroll-Api-Key";
  public static final String HEADER_APP_ID = "X-Divroll-App-Id";
  public static final String HEADER_APP_NAME = "X-Divroll-App-Name";
  public static final String HEADER_AUTH_TOKEN = "X-Divroll-Auth-Token";
  public static final String HEADER_ACL_READ = "X-Divroll-ACL-Read";
  public static final String HEADER_ACL_WRITE = "X-Divroll-ACL-Write";
  public static final String HEADER_ACCEPT = "Accept";
  public static final String HEADER_CONTENT_TYPE = "Content-Type";
  public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
  public static final String HEADER_MASTER_TOKEN = "X-Divroll-Master-Token";
  public static final String HEADER_NAMESPACE = "X-Divroll-Namespace";
  public static final String HEADER_API_ARG = "X-Divroll-Api-Arg";

  public static final String SUPERUSER = "superuser";
  public static final String MASTER_KEY = "masterKey";
  public static final String API_KEY = "apiKey";
  public static final String APP_ID = "appId";
  public static final String APP_NAME = "appName";
  public static final String DOMAIN_NAME = "domainName";
  public static final String ENTITY = "entity";
  public static final String ENTITY_TYPE = "entityType";
  public static final String USER_ID = "userId";
  public static final String ROLE_ID = "roleId";
  public static final String ROLE_NAME = "name";
  public static final String ROLE_LINKNAME = "role"; // TODO add to app.properties file instead
  public static final String USERS_LINKNAME = "users";
  public static final String WEBTOKEN = "authToken";
  public static final String QUERY_USERNAME = "username";
  public static final String QUERY_PASSWORD = "password";
  public static final String QUERY_AUTH_TOKEN = "authToken";
  public static final String QUERY_SKIP = "skip";
  public static final String QUERY_LIMIT = "limit";
  public static final String QUERY_COUNT = "count";
  public static final String JWT_ID_EMAIL = "email";
  public static final String JWT_ID_PASSWORD = "password";
  public static final String JWT_ID_KEY = "id";
  public static final String JWT_ID_EXPIRATION = "expiration";
  public static final String DEFAULT_CHARSET = "utf-8";
  public static final String ACL_ASTERISK = "*";

  public static final String ENTITYSTORE_DOMAIN = "Domain";
  public static final String ENTITYSTORE_APPLICATION = "Application";
  public static final String ENTITYSTORE_FUNCTION = "Function"; // TODO: Remove
  public static final String ENTITYSTORE_CUSTOMCODE = "CustomCode";
  public static final String CUSTOMCODE_NAME = "customCodeName";

  public static final String RESERVED_FIELD_ENTITY_ID = "entityId";
  public static final String RESERVED_FIELD_BLOBNAMES = "blobNames";
  public static final String RESERVED_FIELD_LINKNAMES = "linkNames";
  public static final String RESERVED_FIELD_LINK = "links";
  public static final String RESERVED_FIELD_ACL_READ = "aclRead";
  public static final String RESERVED_FIELD_ACL_WRITE = "aclWrite";
  public static final String RESERVED_FIELD_PUBLICWRITE = "publicWrite";
  public static final String RESERVED_FIELD_PUBLICREAD = "publicRead";
  public static final String RESERVED_FIELD_USERNAME = "username";
  public static final String RESERVED_FIELD_PASSWORD = "password";
  public static final String RESERVED_FIELD_EMAIL = "email";
  public static final String RESERVED_FIELD_ACTIVE = "active";
  public static final String RESERVED_FIELD_FUNCTION_NAME = "functionName";
  public static final String RESERVED_FIELD_METADATA = "metaData";
  public static final String RESERVED_DESTINATION_FILE = "destinationFile";
  public static final String RESERVED_SOURCE_FILE = "sourceFile";
  public static final String RESERVED_FILE_ID = "fileId";
  public static final String RESERVED_OPERATION = "op";
  public static final String RESERVED_OPERATION_MOVE = "move";


  public static final String RESERVED_FIELD_DATE_CREATED = "dateCreated";
  public static final String RESERVED_FIELD_DATE_UPDATED = "dateUpdated";

  public static final String ERROR_MISSING_AUTH_TOKEN = "Missing auth token";
  public static final String ERROR_INVALID_AUTH_TOKEN = "Invalid auth token";
  public static final String ERROR_INVALID_ACL = "Invalid ACL";
  public static final String ERROR_MISSING_USERNAME_PASSWORD = "Missing username/password pair";
  public static final String ERROR_USER_NOT_EXISTS = "User does not exists";
  public static final String ERROR_USERNAME_EXISTS = "Username already exists";
  public static final String ERROR_MISSING_ROLE_ID = "Missing Role ID in request";
  public static final String ERROR_MISSING_USER_ID = "Missing User ID in path";
  public static final String ERROR_ENTITY_WAS_REMOVED = "Entity was removed";
  public static final String ERROR_APPLICATION_NOT_FOUND = "Application not found";
  public static final String ERROR_CANNOT_DELETE_USER = "Cannot delete user or user does not exist";
  public static final String ERROR_QUERY_USERNAME_REQUIRED = "Query parameter username is required";
  public static final String ERROR_MASTERKEY_INVALID = "Invalid Application ID and/or Master Key";
  public static final String ERROR_MASTERKEY_MISSING = "Missing Application ID or Master Key";

  private Constants() {}
}
