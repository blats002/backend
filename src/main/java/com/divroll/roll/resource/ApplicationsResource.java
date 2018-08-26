package com.divroll.roll.resource;

import org.restlet.resource.Get;
import com.divroll.roll.model.Applications;

public interface ApplicationsResource {
    @Get
    public Applications list();
}
