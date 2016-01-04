package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.client.shared.Files;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.FilesResource;
import com.divroll.webdash.server.service.FileService;
import com.divroll.webdash.server.service.WebTokenService;
import com.google.inject.Inject;

import java.util.logging.Logger;

public class GaeFilesServerResource extends SelfInjectingServerResource
        implements FilesResource {

    private static final Logger LOG
            = Logger.getLogger(GaeFilesServerResource.class.getName());

    @Inject
    WebTokenService webTokenService;

    @Inject
    FileService fileService;

    @Override
    public Files list() {
        return null;
    }
}
