package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.shared.Values;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.ValuesResource;
import com.divroll.webdash.server.service.ValueService;
import com.divroll.webdash.server.service.WebTokenService;
import com.google.inject.Inject;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeValuesServerResource extends SelfInjectingServerResource
        implements ValuesResource {

    private static final Logger LOG
            = Logger.getLogger(GaeValuesServerResource.class.getName());

    @Inject
    WebTokenService webTokenService;

    @Inject
    ValueService valueService;

    @Override
    public Values list() {
        return null;
    }
}
