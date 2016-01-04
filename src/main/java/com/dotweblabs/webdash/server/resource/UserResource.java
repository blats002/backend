package com.divroll.webdash.server.resource;

import com.divroll.webdash.client.shared.User;
import org.restlet.resource.Get;

/**
 * Created by Kerby on 10/6/2015.
 */
public interface UserResource {
    @Get
    User getUser();
}
