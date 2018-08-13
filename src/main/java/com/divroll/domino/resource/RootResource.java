package com.divroll.domino.resource;

import com.divroll.domino.model.Server;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.restlet.resource.Get;

@Api(value = "Default", description = "Default Resource")
public interface RootResource {
    @ApiOperation(value = "retrieve server info", tags = "server")
    @ApiResponses({
            @ApiResponse(code = 200, message = "the server info")})
    @Get("json")
    public Server represent();
}
