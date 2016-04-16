package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.shared.Users;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.UsersResource;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeUsersServerResource extends SelfInjectingServerResource
        implements UsersResource {

    private static final Logger LOG
            = Logger.getLogger(GaeUsersServerResource.class.getName());

    @Override
    public Users list() {
        return null;
    }
}
