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
package com.divroll.backend.service.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.helper.Comparables;
import com.divroll.backend.helper.EntityIterables;
import com.divroll.backend.helper.JSON;
import com.divroll.backend.model.*;
import com.divroll.backend.model.filter.TransactionFilter;
import com.divroll.backend.service.ApplicationService;
import com.divroll.backend.xodus.XodusStore;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityId;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeApplicationService implements ApplicationService {

  private static final Logger LOG = LoggerFactory.getLogger(JeeApplicationService.class);

  @Inject
  @Named("masterStore")
  String masterStore;

  @Inject
  XodusStore store;

  @Override
  public EntityId create(Application application, Superuser superuser) {
    Map<String, Comparable> comparableMap = new LinkedHashMap<>();
    comparableMap.put(Constants.MASTER_KEY, application.getMasterKey());
    comparableMap.put(Constants.API_KEY, application.getApiKey());
    comparableMap.put(Constants.APP_ID, application.getAppId());
    if (application.getAppName() != null) {
      comparableMap.put(Constants.APP_NAME, application.getAppName());
    }

    Map<String,String> links = new LinkedHashMap<>();
    if(superuser != null) {
      links.put("superuser", superuser.getEntityId());
    }

    EntityId entityId =
        store.putIfNotExists(masterStore, null,
                Constants.ENTITYSTORE_APPLICATION, comparableMap, links, null, Constants.APP_NAME);
    return entityId;
  }

  @Override
  public Application read(String applicationId) {
    EntityId id =
        store.getFirstEntityId(
            masterStore,
            null,
            Constants.ENTITYSTORE_APPLICATION,
            Constants.APP_ID,
            applicationId,
            String.class);
    if (id != null) {
      Map<String, Comparable> entityMap = store.get(masterStore, null, id.toString());
      if (entityMap != null) {
        Application application = new Application();
        application.setAppId((String) entityMap.get(Constants.APP_ID));
        application.setApiKey((String) entityMap.get(Constants.API_KEY));
        application.setMasterKey((String) entityMap.get(Constants.MASTER_KEY));
        application.setAppName((String) entityMap.get(Constants.APP_NAME));
        application.setCloudCode((String) entityMap.get("cloudCode"));
        EmbeddedEntityIterable embeddedEntityIterable =
            (entityMap.get("emailConfig") != null
                ? (EmbeddedEntityIterable) entityMap.get("emailConfig")
                : null);
        if (embeddedEntityIterable != null) {
          JSONObject jsonObject = EntityIterables.toJSONObject(embeddedEntityIterable);
          Email emailConfg = new Email();
          emailConfg.fromJSONObject(jsonObject);
          application.setEmailConfig(emailConfg);
        }
        String superuserId = (String) entityMap.get("superuser");
        Superuser superuser = new Superuser();
        superuser.setEntityId(superuserId);
        application.setSuperuser(superuser);
        return application;
      }
    }
    return null;
  }

  @Override
  public Application readByName(String appName) {
    EntityId id =
            store.getFirstEntityId(
                    masterStore,
                    null,
                    Constants.ENTITYSTORE_APPLICATION,
                    Constants.APP_NAME,
                    appName,
                    String.class);
    if (id != null) {
      Map<String, Comparable> entityMap = store.get(masterStore, null, id.toString());
      if (entityMap != null) {
        Application application = new Application();
        application.setAppId((String) entityMap.get(Constants.APP_ID));
        application.setApiKey((String) entityMap.get(Constants.API_KEY));
        application.setMasterKey((String) entityMap.get(Constants.MASTER_KEY));
        application.setAppName((String) entityMap.get(Constants.APP_NAME));
        application.setCloudCode((String) entityMap.get("cloudCode"));
        EmbeddedEntityIterable embeddedEntityIterable =
                (entityMap.get("emailConfig") != null
                        ? (EmbeddedEntityIterable) entityMap.get("emailConfig")
                        : null);
        if (embeddedEntityIterable != null) {
          JSONObject jsonObject = EntityIterables.toJSONObject(embeddedEntityIterable);
          Email emailConfg = new Email();
          emailConfg.fromJSONObject(jsonObject);
          application.setEmailConfig(emailConfg);
        }
        String superuserId = (String) entityMap.get("superuser");
        Superuser superuser = new Superuser();
        superuser.setEntityId(superuserId);
        application.setSuperuser(superuser);
        return application;
      }
    }
    return null;
  }

  @Override
  public Application readByDomainName(String domainName) {
    EntityId id =
            store.getFirstEntityId(
                    masterStore,
                    null,
                    Constants.ENTITYSTORE_DOMAIN,
                    Constants.DOMAIN_NAME,
                    domainName,
                    String.class);
    if (id != null) {
      Map<String, Comparable> entityMap = store.get(masterStore, null, id.toString());
      String appName = (String) entityMap.get(Constants.APP_NAME);
      String superuserId = (String) entityMap.get(Constants.SUPERUSER);
      Superuser superuser = new Superuser();
      superuser.setEntityId(superuserId);
      if(entityMap != null) {
        Domain domain = new Domain(id.toString(), domainName, appName, superuser);
        Application application = readByName(domain.getAppName());
        return application;
      }
    }
    return null;
  }

  @Override
  public void update(Application application, String theMasterKey) {
    Map<String, Comparable> comparableMap = new LinkedHashMap<>();
    comparableMap.put(Constants.APP_ID, application.getAppId());
    comparableMap.put(Constants.API_KEY, application.getApiKey());
    comparableMap.put(Constants.MASTER_KEY, application.getMasterKey());
    if (application.getAppName() != null) {
      comparableMap.put(Constants.APP_NAME, application.getAppName());
    }
    if (application.getEmailConfig() != null) {
      JSONObject jsonObject = application.getEmailConfig().toJSONObject();
      EmbeddedEntityIterable embeddedEntityIterable =
          new EmbeddedEntityIterable(Comparables.cast(JSON.jsonToMap(jsonObject)));
      comparableMap.put("emailConfig", embeddedEntityIterable);
    }
    if (application.getCloudCode() != null) {
      comparableMap.put("cloudCode", application.getCloudCode());
    }
    EntityId entityId =
        store.getFirstEntityId(
            masterStore,
            null,
            Constants.ENTITYSTORE_APPLICATION,
            Constants.MASTER_KEY,
            theMasterKey,
            String.class);
    store.update(
        masterStore, null, Constants.ENTITYSTORE_APPLICATION, entityId.toString(), comparableMap);
  }

  @Override
  public void delete(String entityId) {
    store.delete(masterStore, Constants.ENTITYSTORE_APPLICATION, entityId);
  }

  @Override
  public List<Application> list(List<TransactionFilter> filters, int skip, int limit, Superuser owner) {
    List<Application> apps = new LinkedList<>();

    Map<String,String> links = new LinkedHashMap<>();
    if(owner != null) {
        links.put("superuser", owner.getEntityId());
    }

    List<Map<String, Comparable>> list =
        store.list(masterStore, null, Constants.ENTITYSTORE_APPLICATION, filters, skip, limit, links, null);
    for (Map entityMap : list) {
      if (entityMap != null) {
        Application application = new Application();
        application.setAppId((String) entityMap.get(Constants.APP_ID));
        application.setApiKey((String) entityMap.get(Constants.API_KEY));
        application.setMasterKey((String) entityMap.get(Constants.MASTER_KEY));
        application.setAppName((String) entityMap.get(Constants.APP_NAME));
        application.setCloudCode((String) entityMap.get("cloudCode"));

        String superuserId = (String) entityMap.get("superuser");
        Superuser superuser = new Superuser();
        superuser.setEntityId(superuserId);
        application.setSuperuser(superuser);

        EmbeddedEntityIterable embeddedEntityIterable =
            (entityMap.get("emailConfig") != null
                ? (EmbeddedEntityIterable) entityMap.get("emailConfig")
                : null);
        if (embeddedEntityIterable != null) {
          JSONObject jsonObject = EntityIterables.toJSONObject(embeddedEntityIterable);
          Email emailConfg = new Email();
          emailConfg.fromJSONObject(jsonObject);
          application.setEmailConfig(emailConfg);
        }
        apps.add(application);
      }
    }
    return apps;
  }

  @Deprecated
  @Override
  public void forceUpdate(Application application) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EntityId attachDomain(String appName, String domainName, Superuser superuser) {

    Map<String,String> links = new LinkedHashMap<>();
    if(superuser != null) {
      links.put("superuser", superuser.getEntityId());
    }

    Map<String, Comparable> comparableMap = new LinkedHashMap<>();
    comparableMap.put(Constants.DOMAIN_NAME, domainName);
    comparableMap.put(Constants.APP_NAME, appName);

    EntityId entityId = store.putIfNotExists(masterStore,
            null,
            Constants.ENTITYSTORE_DOMAIN,
            comparableMap,
            links,
            null,
            Constants.DOMAIN_NAME);

    return entityId;

  }

  @Override
  public boolean detachDomain(String appName, String domainName, Superuser superuser) {
    Map<String,String> links = new LinkedHashMap<>();
    if(superuser != null) {
      links.put("superuser", superuser.getEntityId());
    }
    return store.delete(masterStore, null, Constants.ENTITYSTORE_DOMAIN, Constants.DOMAIN_NAME, domainName);
  }

}
