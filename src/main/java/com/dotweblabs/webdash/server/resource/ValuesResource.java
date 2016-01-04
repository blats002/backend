package com.divroll.webdash.server.resource;

import com.divroll.webdash.client.shared.Values;
import org.restlet.resource.Get;

import java.util.List;

/**
 * Created by Kerby on 1/5/2016.
 */
public interface ValuesResource {
    @Get
    Values list();
}
