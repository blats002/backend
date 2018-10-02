package com.divroll.backend.resource;

import com.divroll.backend.model.Application;
import org.restlet.resource.Get;
import com.divroll.backend.model.Applications;
import org.restlet.resource.Put;

public interface ApplicationsResource {
    @Get
    public Applications list();
}
