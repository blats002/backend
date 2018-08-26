/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.roll.resource;

import com.divroll.roll.model.Application;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@Api(value = "Application", description = "Application resource")
public interface ApplicationResource {

    @ApiOperation(value = "getEnvironment a new application", tags = "application")
    @ApiResponses({@ApiResponse(code = 200, message = "the application", response = Application.class),})
    @Get
    Application getApp();

    @ApiOperation(value = "update a existing application", tags = "application")
    @ApiResponses({
            @ApiResponse(code = 200, message = "the updated application"),
            @ApiResponse(code = 404, message = "application not found"),
            @ApiResponse(code = 401, message = "unauthorized access")})
    @Put
    Application updateApp(Application application);
}
