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
package com.divroll.backend.repository.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.builder.EntityClassBuilder;
import com.divroll.backend.model.builder.EntityMetadataBuilder;
import com.divroll.backend.repository.CustomCodeRepository;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.xodus.XodusStore;
import com.google.inject.Inject;
import com.google.inject.name.Named;

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
  public String createCustomCode(String appId, String namespace, String customCodeName, String description, Long timeout, InputStream jar) {
    Map<String, Comparable> comparableMap = new LinkedHashMap<String, Comparable>();
    comparableMap.put("customCodeName", customCodeName);
    comparableMap.put("description", description);
    comparableMap.put("timeout", timeout);
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

  @Override
  public List<Map<String, Comparable>> listCustomCodes(String appId, String namespace, String userId) {
    List<Map<String, Comparable>> entityObjs = entityRepository.listEntities(appId, namespace, Constants.ENTITYSTORE_CUSTOMCODE,
            userId, 0, 100, "customCodename", null, null, false, null);
    return entityObjs;
  }

}
