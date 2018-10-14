package com.divroll.backend.resource;

import com.divroll.backend.model.Application;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.restlet.resource.Get;
import com.divroll.backend.model.Applications;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

public interface ApplicationsResource {
    @ApiOperation(value = "getEnvironment a new application", tags = "application")
    @ApiResponses({@ApiResponse(code = 200, message = "the application", response = Application.class),})
    @Post
    Application createApp(Application rootDTO);
    @Get
    public Applications list();
}
