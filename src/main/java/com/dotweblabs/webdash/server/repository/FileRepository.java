package com.divroll.webdash.server.repository;

import com.divroll.webdash.client.shared.File;
import com.divroll.webdash.client.shared.Files;

public interface FileRepository extends CrudRepository<File> {
    public Files list(String cursor);
}
