/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
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
import org.restlet.resource.Post;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface ApplicationsResource {
  @ApiOperation(value = "Create a new application", tags = "application")
  @ApiResponses({
    @ApiResponse(code = 200, message = "the application", response = Application.class),
  })
  @Post
  Application createApp(Application rootDTO);

  @ApiOperation(value = "List applications", tags = "application")
  @ApiResponses({
          @ApiResponse(code = 200, message = "list of applications", response = Application.class),
  })
  @ApiImplicitParams({
          @ApiImplicitParam(name = "masterToken", value = "Master Token", required = true, dataType = "string", paramType = "query"),
          @ApiImplicitParam(name = "masterKey", value = "Master Key", required = false, dataType = "string", paramType = "query"),
          @ApiImplicitParam(name = "skip", value = "Skip", required = false, dataType = "integer", paramType = "query"),
          @ApiImplicitParam(name = "limit", value = "Limit", required = false, dataType = "integer", paramType = "query"),
  })
  @Get
  public Representation list();
}
