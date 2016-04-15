package com.divroll.webdash.server.repository;

import com.divroll.webdash.shared.File;
import com.divroll.webdash.shared.Files;

public interface FileRepository extends CrudRepository<File> {
    public Files list(String cursor);
}
