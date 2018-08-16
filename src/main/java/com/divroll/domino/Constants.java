package com.divroll.domino;

public final class Constants {
    public static final String SERVER_NAME = "Domino";
    public static final String HEADER_MASTER_KEY = "X-Domino-Master-Key";
    public static final String HEADER_API_KEY = "X-Domino-Api-Key";
    public static final String HEADER_APP_ID = "X-Domino-App-Id";
    public static final String HEADER_AUTH_TOKEN = "X-Domino-Auth-Token";
    public static final String HEADER_ACL_READ = "X-Domino-ACL-Read";
    public static final String HEADER_ACL_WRITE = "X-Domino-ACL-Write";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String MASTER_KEY = "masterKey";
    public static final String API_KEY = "apiKey";
    public static final String APP_ID = "appId";
    public static final String ENTITY_ID = "entityId";
    public static final String ENTITY_TYPE = "entityType";
    public static final String USER_ID = "userId";
    public static final String ROLE_ID = "roleId";
    public static final String ROLE_NAME = "name";
    public static final String ROLE_LINKNAME = "role";
    public static final String WEBTOKEN = "webToken";
    public static final String QUERY_USERNAME = "username";
    public static final String QUERY_PASSWORD = "password";
    public static final String QUERY_SKIP = "skip";
    public static final String QUERY_LIMIT = "limit";
    public static final String JWT_ID_KEY = "id";
    public static final String DEFAULT_CHARSET = "utf-8";
    public static final String ACL_ASTERISK = "*";
    public static final String ENTITYSTORE_APPLICATION = "Application";
    public static final String METADATA_KEY = "_md";
    public static final String BLOBNAMES = "blobnames";
    public static final String LINKS = "links";
    public static final String ACL_READ = "aclRead";
    public static final String ACL_WRITE = "aclWrite";
    public static final String RESERVED_FIELD_PUBLICWRITE = "publicWrite";
    public static final String RESERVED_FIELD_PUBLICREAD = "publicRead";
    public static final String RESERVED_FIELD_USERNAME = "username";
    public static final String RESERVED_FIELD_PASSWORD = "password";
    public static final String ERROR_MISSING_AUTH_TOKEN = "Missing auth token";
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
