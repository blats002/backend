package com.divroll.domino.resource.jee;

import com.divroll.domino.model.Application;
import com.divroll.domino.model.Applications;
import com.divroll.domino.resource.ApplicationsResource;
import com.divroll.domino.service.ApplicationService;
import com.divroll.domino.xodus.XodusStore;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.List;

public class JeeApplicationsServerResource extends BaseServerResource
        implements ApplicationsResource {

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    XodusStore store;

    @Inject
    ApplicationService applicationService;

    @Override
    public Applications list() {
        // TODO: Add auth
        List<Application> results = applicationService.list();
        Applications applications = new Applications();
        applications.setSkip(0);
        applications.setLimit(0);
        applications.setResults(results);
        return applications;
    }
}
