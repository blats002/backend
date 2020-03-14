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

import com.divroll.backend.model.User;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@Api(value = "User", description = "User resource")
public interface UserResource {
  @ApiOperation(value = "retrieve a authToken of user", tags = "user")
  @ApiResponses({
    @ApiResponse(code = 200, message = "the updated application"),
    @ApiResponse(code = 404, message = "user not found"),
    @ApiResponse(
        code = 401,
        message =
            "unauthorized access, wrong username/password pair or missing Appliation ID/API Key headers pair")
  })
  @Get
  Representation getUser();

  @ApiOperation(value = "update existing user", tags = "user")
  @ApiResponses({
    @ApiResponse(code = 200, message = "user updated"),
    @ApiResponse(code = 400, message = "bad request, no payload or username already exists"),
    @ApiResponse(
        code = 401,
        message =
            "unauthorized access, missing Application ID/API Key headers pair or missing Authentication Token")
  })
  @Put
  Representation updateUser(User entity);

  @ApiOperation(value = "delete existing user", tags = "user")
  @ApiResponses({
    @ApiResponse(code = 200, message = "user deleted"),
    @ApiResponse(
        code = 401,
        message = "unauthorized access, missing Application ID/API Key/Master Key headers")
  })
  @Delete
  void deleteUser(User entity);
}
