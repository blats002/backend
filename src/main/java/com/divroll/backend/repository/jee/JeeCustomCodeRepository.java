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
package com.divroll.backend.repository.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.builder.EntityClassBuilder;
import com.divroll.backend.model.builder.EntityMetadataBuilder;
import com.divroll.backend.repository.CustomCodeRepository;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.xodus.XodusStore;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityId;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeCustomCodeRepository implements CustomCodeRepository {

  @Inject
  @Named("xodusRoot")
  String xodusRoot;

  @Inject
  @Named("masterStore")
  String masterStore;

  @Inject XodusStore store;

  @Inject EntityRepository entityRepository;

  @Override
  public String createCustomCode(String appId, String namespace, String customCodeName, InputStream jar) {
    Map<String, Comparable> comparableMap = new LinkedHashMap<String, Comparable>();
    comparableMap.put("customCodeName", customCodeName);
    String entityId = entityRepository.createEntity(
            appId,
            namespace,
            Constants.ENTITYSTORE_CUSTOMCODE,
            new EntityClassBuilder()
                    .comparableMap(comparableMap)
                    .publicRead(false)
                    .publicWrite(false)
                    .blobName("jar")
                    .blob(jar)
                    .build(), null, null, null,
            new EntityMetadataBuilder().addUniqueProperties("customCodeName").build());
    return entityId;
  }

  @Override
  public boolean deleteCustomCode(String appId, String namespace, String customCodeName) {
    return entityRepository.deleteEntities(appId, namespace, Constants.ENTITYSTORE_CUSTOMCODE, "customCodeName", customCodeName);
  }

  @Override
  public InputStream getCustomCode(String appId, String namespace, String customCodeName) {
    InputStream is =
        entityRepository.getFirstEntityBlob(
            appId,
            namespace,
            Constants.ENTITYSTORE_CUSTOMCODE,
            "customCodeName",
            customCodeName,
            String.class,
            "jar");
    return is;
  }

  @Override
  public List<InputStream> getAfterSavedLinkedCustomCodes(String appId, String namespace, String entityType) {
    //entityRepository.getLinkedEntities(appId, namespace, Constants.ENTITYSTORE_CUSTOMCODE, "id", "afterSave");
    return null;
  }

  @Override
  public List<InputStream> getBeforeSavedLinkedCustomCodes(String appId, String namespace, String entityType) {
    return null;
  }

  @Override
  public Map<String, Comparable> getCustomCodeMeta(String appId, String namespace, String customCodeName) {
    List<Map<String,Comparable>> comparables
            = entityRepository.getEntity(appId, namespace, Constants.ENTITYSTORE_CUSTOMCODE, Constants.CUSTOMCODE_NAME, customCodeName, new ArrayList<>());
    for(Map<String,Comparable> comparable : comparables) {
      return comparable;
    }
    return null;
  }

}
