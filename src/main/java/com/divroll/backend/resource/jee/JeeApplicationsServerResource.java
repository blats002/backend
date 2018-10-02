package com.divroll.backend.resource.jee;

import com.divroll.backend.model.Application;
import com.divroll.backend.model.Applications;
import com.divroll.backend.model.Email;
import com.divroll.backend.resource.ApplicationsResource;
import com.divroll.backend.service.ApplicationService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;

import java.util.LinkedList;
import java.util.List;

public class JeeApplicationsServerResource extends BaseServerResource
        implements ApplicationsResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeApplicationsServerResource.class);

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
        try {
            // TODO: Add auth
            if(theMasterToken != null
                    && masterToken != null
                    && BCrypt.checkpw(masterToken, theMasterToken)) {
                List<Application> results = applicationService.list(filters, skip, limit);
                Applications applications = new Applications();
                applications.setSkip(skip);
                applications.setLimit(limit);
                applications.setResults(results);
                setStatus(Status.SUCCESS_OK);
                return applications;
            } else if(isMaster()) {
                Applications applications = new Applications();
                Application application = applicationService.read(appId);
                applications.getResults().add(application);
                applications.setSkip(skip);
                applications.setLimit(1L);
                setStatus(Status.SUCCESS_OK);
                return applications;
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

}
