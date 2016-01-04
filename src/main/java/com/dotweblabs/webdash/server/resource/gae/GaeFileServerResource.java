package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.client.shared.File;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.FileResource;
import com.divroll.webdash.server.service.FileService;
import com.divroll.webdash.server.service.WebTokenService;
import com.google.inject.Inject;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeFileServerResource extends SelfInjectingServerResource
        implements FileResource {

    private static final Logger LOG
            = Logger.getLogger(GaeFileServerResource.class.getName());

    @Inject
    WebTokenService webTokenService;

    @Inject
    FileService fileService;

    @Override
    public File getFile() {
        return null;
    }
}
