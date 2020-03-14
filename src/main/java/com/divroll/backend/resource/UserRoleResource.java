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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@Api(value = "User", description = "User Role link resource")
public interface UserRoleResource {
  @ApiOperation(value = "", tags = "user")
  @ApiResponses({
    @ApiResponse(code = 201, message = "user role link created"),
    @ApiResponse(code = 400, message = "bad request"),
    @ApiResponse(code = 401, message = "unauthorized access")
  })
  @Post
  void createUserRoleLink(Representation entity);

  @ApiOperation(value = "", tags = "user")
  @ApiResponses({
    @ApiResponse(code = 200, message = "deleted"),
    @ApiResponse(code = 400, message = "bad request"),
    @ApiResponse(code = 401, message = "unauthorized access")
  })
  @Delete
  void deleteUserRoleLink(Representation entity);
}
