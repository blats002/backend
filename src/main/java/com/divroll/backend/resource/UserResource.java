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
package com.divroll.backend.resource;

import com.divroll.backend.model.UserDTO;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
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
  @ApiOperation(value = "retrieve a webToken of user", tags = "user")
  @ApiResponses({
    @ApiResponse(code = 200, message = "the updated application"),
    @ApiResponse(code = 404, message = "user not found"),
    @ApiResponse(
        code = 401,
        message =
            "unauthorized access, wrong username/password pair or missing Appliation ID/API Key headers pair")
  })
  @Get
  UserDTO getUser();

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
  UserDTO updateUser(UserDTO entity);

  @ApiOperation(value = "delete existing user", tags = "user")
  @ApiResponses({
    @ApiResponse(code = 200, message = "user deleted"),
    @ApiResponse(
        code = 401,
        message = "unauthorized access, missing Application ID/API Key/Master Key headers")
  })
  @Delete
  void deleteUser(UserDTO entity);
}
