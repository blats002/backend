package com.divroll.bucket.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;

/**
 * Created by Kerby on 7/10/2017.
 */
public interface CopyFromParseToGoogleCloud {
    @Get
    Representation get(Representation entity);
}
