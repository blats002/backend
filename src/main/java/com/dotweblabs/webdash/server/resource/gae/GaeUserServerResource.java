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

    String username;

    @Override
    protected void doInit() {
        super.doInit();
        username = getAttribute("username");
    }

    @Override
    public User getUser() {
        try{
            String token = getQueryValue("token");
            if(token != null && !token.equals("")){
                User ref = userService.read(username);
                Long userId = webTokenService.readUserIdFromToken(token);
                if(ref.getId().equals(userId)) {
                    return ref;
                } else {
                    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                }
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
