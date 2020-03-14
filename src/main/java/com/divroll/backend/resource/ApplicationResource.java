/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
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
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * GPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.resource;

import com.divroll.backend.model.Application;
import com.wordnik.swagger.annotations.*;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@Api(value = "Application", description = "Application resource")
public interface ApplicationResource {

  @ApiOperation(value = "Create a new application", tags = "application")
  @ApiResponses({
    @ApiResponse(code = 200, message = "the application", response = Application.class),
  })
  @ApiImplicitParams({
          @ApiImplicitParam(name = "masterKey", value = "Master Key", required = true, dataType = "string", paramType = "query")
  })
  @Post
  Application createApp(Application rootDTO);

  @ApiOperation(value = "update a existing application", tags = "application")
  @ApiResponses({
    @ApiResponse(code = 200, message = "the updated application"),
    @ApiResponse(code = 404, message = "application not found"),
    @ApiResponse(code = 401, message = "unauthorized access")
  })
  @ApiImplicitParams({
          @ApiImplicitParam(name = "appName", value = "Application name", required = true, dataType = "string", paramType = "query")
  })
  @Put
  Application updateApp(Application entity);

  @Get
  Representation retrieveApp();
}
