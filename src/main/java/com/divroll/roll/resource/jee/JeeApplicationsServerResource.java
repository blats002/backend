package com.divroll.roll.resource.jee;

import com.divroll.roll.model.Application;
import com.divroll.roll.model.Applications;
import com.divroll.roll.resource.ApplicationsResource;
import com.divroll.roll.service.ApplicationService;
import com.divroll.roll.xodus.XodusStore;
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
