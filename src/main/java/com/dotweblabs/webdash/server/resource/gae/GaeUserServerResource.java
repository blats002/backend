package com.divroll.webdash.server.resource.gae;

import com.google.inject.Inject;
import com.divroll.webdash.client.shared.User;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.UserResource;
import com.divroll.webdash.server.service.UserService;
import com.divroll.webdash.server.service.WebTokenService;
import com.divroll.webdash.server.service.exception.ValidationException;
import org.restlet.data.Status;

import java.util.logging.Logger;

public class GaeUserServerResource extends SelfInjectingServerResource
    implements UserResource {

    private static final Logger LOG
            = Logger.getLogger(GaeUserServerResource.class.getName());

    @Inject
    WebTokenService webTokenService;

    @Inject
    UserService userService;

    @Override
    public User getUser() {
        String token = getQueryValue("token");
        if(token != null && !token.equals("")){
            String userId = webTokenService.readUserIdFromToken(token);
            try {
                User user = userService.read(userId);
                return user;
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        return null;
    }
}
