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
 */
package com.divroll.backend.resource;

import com.divroll.backend.model.Application;
import com.wordnik.swagger.annotations.*;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface BackupResource {

  @ApiOperation(value = "Restore database from Zip file", tags = "restore")
  @ApiResponses({
          @ApiResponse(code = 202, message = ""),
  })
  @ApiImplicitParams({
          @ApiImplicitParam(name = "masterKey", value = "Master Key", required = true, dataType = "string", paramType = "query")
  })
  @Post
  void restore(Representation entity);

  @ApiOperation(value = "Backup and download database", tags = "backup")
  @ApiResponses({
          @ApiResponse(code = 200, message = "Zip file of the database", response = FileRepresentation.class),
  })
  @ApiImplicitParams({
          @ApiImplicitParam(name = "masterKey", value = "Master Key", required = true, dataType = "string", paramType = "query")
  })
  @Get
  Representation backup(Representation entity);

}
