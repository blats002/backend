package com.divroll.backend.resource;

import com.divroll.backend.model.Function;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

public interface ManageFunctionResource {
    @Get
    Function createFunction(Function entity);
    @Delete
    void removeFunction(Function entity);
}
