package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.shared.Subdomain;
import com.divroll.webdash.shared.Subdomains;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.SubdomainsResource;
import com.divroll.webdash.server.service.SubdomainService;
import com.divroll.webdash.server.service.UserService;
import com.divroll.webdash.server.service.WebTokenService;
import com.google.inject.Inject;
import org.restlet.data.Status;

import java.util.logging.Logger;

public class GaeSubdomainsServerResource extends SelfInjectingServerResource
    implements SubdomainsResource {

    private static final Logger LOG
            = Logger.getLogger(GaeSubdomainsServerResource.class.getName());

    @Inject
    SubdomainService subdomainService;

    @Inject
    UserService userService;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Subdomains list() {
        try{
            String token = getQueryValue("token");
            if(token != null && !token.equals("")){
                Long userId = webTokenService.readUserIdFromToken(token);
                if(userId != null) {
                    setStatus(Status.SUCCESS_OK);
                    return subdomainService.findByUser(userId);
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

    @Override
    public Subdomain createSubdomain(Subdomain subdomain) {
        Subdomain result = null;
        try{
            String token = getQueryValue("token");
            if(token != null && !token.equals("") && subdomain != null){
                Long userId = webTokenService.readUserIdFromToken(token);
                LOG.info("User id: " + userId);
                if(userId != null && userId.equals(subdomain.getUserId())) {
                    result = subdomainService.create(subdomain);
                    setStatus(Status.SUCCESS_OK);
                } else {
                    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                }
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } catch (Exception e){
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return result;
    }
}
