package com.divroll.roll.resource.jee;

import com.divroll.roll.model.Application;
import com.divroll.roll.model.Applications;
import com.divroll.roll.resource.ApplicationsResource;
import com.divroll.roll.service.ApplicationService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;

import java.util.List;

public class JeeApplicationsServerResource extends BaseServerResource
        implements ApplicationsResource {

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    @Named("masterToken")
    String theMasterToken;

    @Inject
    ApplicationService applicationService;

    @Override
    public Applications list() {
        // TODO: Add auth
        if(theMasterToken != null
                && masterToken != null
                && BCrypt.checkpw(masterToken, theMasterToken)) {
            List<Application> results = applicationService.list();
            Applications applications = new Applications();
            applications.setSkip(0);
            applications.setLimit(0);
            applications.setResults(results);
            return applications;
        } else {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
        return null;
    }
}
