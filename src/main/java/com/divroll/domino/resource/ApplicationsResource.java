package com.divroll.domino.resource;

import org.restlet.resource.Get;
import com.divroll.domino.model.Applications;
import java.util.List;

public interface ApplicationsResource {
    @Get
    public Applications list();
}
