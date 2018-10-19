package com.divroll.backend.resource;


import com.divroll.backend.model.Configuration;
import org.restlet.resource.Get;

public interface ConfigurationResource {
    @Get
    Configuration getConfiguration();
}
