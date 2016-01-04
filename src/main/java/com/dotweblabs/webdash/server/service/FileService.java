package com.divroll.webdash.server.service;

import com.divroll.webdash.client.shared.File;
import com.divroll.webdash.client.shared.Files;
import com.divroll.webdash.server.service.exception.ValidationException;

/**
 * Created by Kerby on 1/5/2016.
 */
public interface FileService {
    public File save(File user) throws ValidationException;
    public File read(Long blogId) throws ValidationException;
    public File update(File user) throws ValidationException;
    public void delete(Long blogId) throws ValidationException;
    public Files list(String cursor) throws ValidationException;
}
