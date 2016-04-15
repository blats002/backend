package com.divroll.webdash.server.resource.gae;

import com.divroll.webdash.shared.Files;
import com.divroll.webdash.server.guice.SelfInjectingServerResource;
import com.divroll.webdash.server.resource.FilesResource;
import com.divroll.webdash.server.service.FileService;
import com.divroll.webdash.server.service.WebTokenService;
import com.divroll.webdash.server.service.exception.ValidationException;
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

    String cursor;

    @Override
    protected void doInit() {
        super.doInit();
        cursor = getQueryValue("cursor");
    }

    @Override
    public Files list() {
        Files files = null;
        try {
            files = fileService.list(cursor);
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        return files;
    }
}
