package com.divroll.webdash.server.resource;

import com.divroll.webdash.shared.Files;
import org.restlet.resource.Get;

/**
 * Created by Kerby on 1/5/2016.
 */
public interface FilesResource {
    @Get
    Files list();
}
