/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019, Divroll, and individual contributors
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
package com.divroll.backend.service;

import com.divroll.backend.model.Application;
import com.divroll.backend.model.action.Action;
import com.divroll.backend.model.action.EntityAction;
import com.divroll.backend.model.builder.CreateOption;
import com.divroll.backend.model.builder.EntityACL;
import com.divroll.backend.model.builder.EntityClass;
import com.divroll.backend.model.builder.EntityMetadata;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface EntityService {

  EntityACL retrieveEntityACLWriteList(Application application,
                                       String namespace,
                                       String entityType,
                                       String propertyName,
                                       Comparable propertyValue);

//  JSONObject createEntities(
//          Application application,
//          String namespace,
//          List<EntityClass> entities);

  JSONObject createEntity(
      Application application,
      String namespace,
      String entityType,
      Map<String, Comparable> comparableMap,
      String aclRead,
      String aclWrite,
      Boolean publicRead,
      Boolean publicWrite,
      List<Action> actions,
      List<EntityAction> entityActions,
      CreateOption createOption,
      EntityMetadata metadata)
      throws Exception;

  JSONObject createEntity(
      Application application,
      String namespace,
      String entityType,
      Map<String, Comparable> comparableMap,
      String aclRead,
      String aclWrite,
      Boolean publicRead,
      Boolean publicWrite,
      List<Action> actions,
      List<EntityAction> entityActions,
      CreateOption createOption,
      String blobName,
      InputStream blobStream,
      EntityMetadata metadata)
      throws Exception;

  boolean beforeSave(
      Application application,
      String namespace,
      Map<String, Comparable> entity,
      String appId,
      String entityType);

  boolean afterSave(
      Application application,
      String namespace,
      Map<String, Comparable> entity,
      String appId,
      String entityType);

  void validateSchema(
      String appId, String namespace, String entityType, Map<String, Comparable> comparableMap)
      throws IllegalArgumentException;
}
