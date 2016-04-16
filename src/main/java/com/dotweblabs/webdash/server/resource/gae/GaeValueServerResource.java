package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.shared.Value;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.ValueResource;
import com.divroll.webdash.server.service.ValueService;
import com.divroll.webdash.server.service.WebTokenService;
import com.google.inject.Inject;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeValueServerResource extends SelfInjectingServerResource
        implements ValueResource{

    private static final Logger LOG
            = Logger.getLogger(GaeValueServerResource.class.getName());

    @Inject
    WebTokenService webTokenService;

    @Inject
    ValueService valueService;

    @Override
    public Value getValue() {
        return null;
    }
}
