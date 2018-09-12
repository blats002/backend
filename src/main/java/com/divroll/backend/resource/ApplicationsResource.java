package com.divroll.backend.resource;

import org.restlet.resource.Get;
import com.divroll.backend.model.Applications;

public interface ApplicationsResource {
    @Get
    public Applications list();
}
