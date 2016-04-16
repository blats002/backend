package com.divroll.webdash.server.resource;

import com.divroll.webdash.shared.Users;
import org.restlet.resource.Get;

/**
 * Created by Kerby on 1/5/2016.
 */
public interface UsersResource {
    @Get
    Users list();
}
