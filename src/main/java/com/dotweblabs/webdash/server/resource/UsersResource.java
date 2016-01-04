package com.divroll.webdash.server.resource;

import com.divroll.webdash.client.shared.Users;
import org.restlet.ext.apispark.internal.agent.bean.User;
import org.restlet.resource.Get;

import java.util.List;

/**
 * Created by Kerby on 1/5/2016.
 */
public interface UsersResource {
    @Get
    Users list();
}
