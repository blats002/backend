package com.divroll.webdash.server.service.gae;

import com.divroll.webdash.client.shared.File;
import com.divroll.webdash.client.shared.Files;
import com.divroll.webdash.server.repository.gae.GaeFileRepository;
import com.divroll.webdash.server.service.FileService;
import com.divroll.webdash.server.service.exception.ValidationException;
import com.google.inject.Inject;

import java.util.logging.Logger;

/**
 * Created by Kerby on 1/5/2016.
 */
public class GaeFileService implements FileService {

    private static final Logger LOG
            = Logger.getLogger(GaeFileService.class.getName());

    @Inject
    GaeFileRepository fileRepository;

    @Override
    public File save(File user) throws ValidationException {
        return null;
    }

    @Override
    public File read(Long blogId) throws ValidationException {
        return null;
    }

    @Override
    public File update(File user) throws ValidationException {
        return null;
    }

    @Override
    public void delete(Long blogId) throws ValidationException {

    }

    @Override
    public Files list(String cursor) throws ValidationException {
        return null;
    }
}
