package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.client.shared.User;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.TokensResource;
import com.divroll.webdash.server.resource.UserResource;
import com.divroll.webdash.server.service.UserService;
import com.divroll.webdash.server.service.WebTokenService;
import com.divroll.webdash.server.service.exception.ValidationException;
import com.divroll.webdash.server.util.Base64;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Status;
import org.restlet.data.Status;
import org.restlet.util.Series;
import org.mindrot.jbcrypt.BCrypt;


import java.util.logging.Logger;

public class GaeTokensServerResource extends SelfInjectingServerResource
    implements TokensResource {

    private static final Logger LOG
            = Logger.getLogger(GaeTokensServerResource.class.getName());

    @Inject
    WebTokenService webTokenService;

    @Inject
    UserService userService;

    String username;
    String password;
    String authValue;

    @Inject
    @Named("bootstrap.username")
    String defaultUsername;

    @Inject
    @Named("bootstrap.password")
    String defaultPassword;

    @Override
    protected void doInit() {
        super.doInit();
        userBootstrap();
        username = getQueryValue("username");
        password = getQueryValue("password");
        Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");
        authValue = headers.getFirstValue("Authorization").replaceFirst("Basic ","");
        String decoded = Base64.decode(authValue);
        String[] usernamePasswordPair = decoded.split(":");
        username = (username == null) ? usernamePasswordPair[0] : username;
        password = (password == null) ? usernamePasswordPair[1] : password;
    }

    @Override
    public String signin() {
        //LOG.info("Authorization: " + authValue);
        try{
            if(username != null
                    && password != null
                    && !username.isEmpty()
                    && !password.isEmpty()){
                User user = userService.read(username);
                if(user != null){
                    String passwordHash = user.getPassword();
                    LOG.info("Password: " + password);
                    if(BCrypt.checkpw(password, passwordHash)){
                        return webTokenService.createToken(user.getId());
                    } else {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Username password is invalid");
                    }
                } else {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND, "User with username " + username + " not found");
                    return null;
                }
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private void userBootstrap(){
        User defaultUser = new User(defaultUsername, BCrypt.hashpw(password, BCrypt.gensalt()));
        try {
            userService.saveNew(defaultUser);
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }
}
