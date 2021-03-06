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
package com.divroll.backend.resource.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.helper.ACLHelper;
import com.divroll.backend.helper.Comparables;
import com.divroll.backend.helper.JSON;
import com.divroll.backend.helper.ObjectLogger;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.EntityStub;
import com.divroll.backend.model.Role;
import com.divroll.backend.model.action.EntityAction;
import com.divroll.backend.model.action.ImmutableBacklinkAction;
import com.divroll.backend.model.action.ImmutableLinkAction;
import com.divroll.backend.model.builder.CreateOption;
import com.divroll.backend.model.builder.CreateOptionBuilder;
import com.divroll.backend.model.builder.EntityACL;
import com.divroll.backend.model.builder.EntityMetadataBuilder;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.resource.BlobResource;
import com.divroll.backend.service.PubSubService;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import com.google.inject.Inject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import util.ComparableLinkedList;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeBlobServerResource extends BaseServerResource implements BlobResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeBlobServerResource.class);

  @Inject EntityRepository entityRepository;

  @Inject RoleRepository roleRepository;

  @Inject WebTokenService webTokenService;

  @Inject PubSubService pubSubService;

  @Override
  protected void doInit() {
    super.doInit();
    if(blobName == null || blobName.isEmpty()) {
      blobName = getQueryValue("blobName");
    }
    try {
      if(blobName != null) {
        blobName = URLDecoder.decode(blobName, "UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Representation setBlob(Representation entity) {

    LOG.info("Method setBlob called");
    LOG.info("Media type - " + entity.getMediaType());
    LOG.info("Media size - " + entity.getSize());

    try {

      if (entity == null || entity.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
      }

      Application app = applicationService.read(appId);
      if (app == null) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return null;
      }

      String authUserId = null;

      boolean isWriteAccess = false;
      boolean isMaster = false;
      boolean isPublic = false;

      try {
        authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      } catch (Exception e) {
        // do nothing
      }

      if (entityId == null || entityId.isEmpty()) { // create a new Entity
        entityType = getQueryValue(Constants.ENTITY_TYPE);

        if (entityType == null) {
          return badRequest();
        }
        String entityJson = getQueryValue("entity");
        try {
          JSONObject jsonObject = new JSONObject(entityJson);
          Map<String, Comparable> comparableMap = JSON.jsonToMap(jsonObject);

          List<EntityAction> entityActions = new LinkedList<>();
          if (linkName != null && linkFrom != null) {
            boolean isAuth = true;
            if (authUserId == null
                || (!entityRepository
                        .getACLWriteList(appId, namespace, linkFrom)
                        .contains(authUserId)
                    && !isMaster())) {
              isAuth = false;
            }
            if (authUserId != null && linkFrom.equals(authUserId)) {
              isAuth = true;
            }
            if (entityRepository.isPublicWrite(appId, namespace, linkFrom)) {
              isAuth = true;
            }
            if (!isAuth) {
              return unauthorized();
            }
            entityActions.add(
                ImmutableBacklinkAction.builder().linkName(linkName).entityId(linkFrom).build());
          } else if (authUserId != null && linkName != null && linkTo != null) {
            if (authUserId == null
                || !entityRepository.getACLWriteList(appId, namespace, linkTo).contains(authUserId)
                    && !isMaster()) {
              return unauthorized();
            }
            entityActions.add(
                ImmutableLinkAction.builder().linkName(linkName).entityId(linkFrom).build());
          }

          if (encoding != null && encoding.equals("base64")) {
            LOG.info("Media type - " + "base64");
            String base64 = entity.getText();
            byte[] bytes = BaseEncoding.base64().decode(base64);
            InputStream inputStream = ByteSource.wrap(bytes).openStream();
            JSONObject entityJSONObject =
                entityService.createEntity(
                    getApp(),
                    namespace,
                    entityType,
                    comparableMap,
                    aclRead,
                    aclWrite,
                    publicRead,
                    publicWrite,
                    new LinkedList<>(),
                    entityActions,
                    null,
                    blobName,
                    inputStream,
                    new EntityMetadataBuilder().uniqueProperties(uniqueProperties).build());
            String entityId = entityJSONObject.getString(Constants.RESERVED_FIELD_ENTITY_ID);
            if (entityJSONObject != null && entityId != null) {
              pubSubService.updated(appId, namespace, entityType, entityId);
              setStatus(Status.SUCCESS_CREATED);
              return created(entityJSONObject);
            } else {
              return badRequest();
            }
          } else {
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
              LOG.info("Media type - " + entity.getMediaType());
              String writeOver = getQueryValue("writeOver");
              CreateOption.CREATE_OPTION createOption = null;
              if(writeOver != null) {
                createOption = CreateOption.CREATE_OPTION.SET_BLOB_ON_PROPERTY_EQUALS;
                EntityACL entityACL = entityService.retrieveEntityACLWriteList(getApp(),
                        namespace, entityType, writeOver, comparableMap.get(writeOver));
                ObjectLogger.log(entityACL);
                if(entityACL != null) {
                  if(!isMaster
                          && !entityACL.write().contains(authUserId)
                          && !entityACL.publicWrite()) {
                    return unauthorized();
                  }
                }
              }

              Request restletRequest = getRequest();
              HttpServletRequest servletRequest = ServletUtils.getRequest(restletRequest);
              ServletFileUpload upload = new ServletFileUpload();
              FileItemIterator fileIterator = upload.getItemIterator(servletRequest);
              while (fileIterator.hasNext()) {
                FileItemStream item = fileIterator.next();
                String fieldName = item.getFieldName();
                String name = item.getName();
                if (item.isFormField()) {
                } else {
                  CountingInputStream countingInputStream =
                      new CountingInputStream(item.openStream());
                  JSONObject wrappedEntityJSON =
                      entityService.createEntity(
                          getApp(),
                          namespace,
                          entityType,
                          comparableMap,
                          aclRead,
                          aclWrite,
                          publicRead,
                          publicWrite,
                          new LinkedList<>(),
                          entityActions,
                          new CreateOptionBuilder()
                                  .createOption(createOption)
                                  .referencePropertyName(writeOver)
                                  .build(),
                          blobName,
                          countingInputStream,
                          new EntityMetadataBuilder().uniqueProperties(uniqueProperties).build());
                  JSONObject entityJSON = wrappedEntityJSON.getJSONObject("entity");
                  String entityId = entityJSON.getString(Constants.RESERVED_FIELD_ENTITY_ID);
                  if (wrappedEntityJSON != null && entityId != null) {
                    pubSubService.updated(appId, namespace, entityType, entityId);
                    return created(wrappedEntityJSON);
                  } else {
                    return badRequest();
                  }
                }
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          return badRequest();
        }
      } else if (entityId != null) {
        Map<String, Comparable> map =
            entityRepository.getEntity(appId, namespace, entityType, entityId, null);
        List<EntityStub> aclWriteList =
            map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
                ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE)
                : new LinkedList<>();

        if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
          isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
        }

        if (isMaster()) {
          isMaster = true;
        } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
          isWriteAccess = true;
        } else if (authUserId != null) {
          List<Role> roles = roleRepository.getRolesOfEntity(appId, namespace, authUserId);
          for (Role role : roles) {
            if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
              isWriteAccess = true;
            }
          }
        }

        if (isMaster || isWriteAccess || isPublic) {
          // TODO: Compress stream
          if (encoding != null && encoding.equals("base64")) {
            String base64 = entity.getText();
            byte[] bytes = BaseEncoding.base64().decode(base64);
            InputStream inputStream = ByteSource.wrap(bytes).openStream();
            LOG.info("Creating entity blob - base64");
            if (entityRepository.createEntityBlob(
                appId, namespace, entityType, entityId, blobName, inputStream)) {
              pubSubService.updated(appId, namespace, entityType, entityId);
              setStatus(Status.SUCCESS_CREATED);
            } else {
              setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
          } else {
            if (entity != null
                && MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
              Request restletRequest = getRequest();
              HttpServletRequest servletRequest = ServletUtils.getRequest(restletRequest);
              ServletFileUpload upload = new ServletFileUpload();
              FileItemIterator fileIterator = upload.getItemIterator(servletRequest);
              LOG.info("File Item iterator - " + fileIterator.hasNext());
              while (fileIterator.hasNext()) {
                FileItemStream item = fileIterator.next();
                String fieldName = item.getFieldName();
                String name = item.getName();
                LOG.info("Item isFormField - " + item.isFormField());
                if (item.isFormField()) {
                } else {
                  CountingInputStream countingInputStream =
                      new CountingInputStream(item.openStream());
                  LOG.info("Creating entity blob - multipart form data - " + item.getContentType());
                  if (entityRepository.createEntityBlob(
                      appId, namespace, entityType, entityId, blobName, countingInputStream)) {
                    pubSubService.updated(appId, namespace, entityType, entityId);
                    setStatus(Status.SUCCESS_CREATED);
                  } else {
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                  }
                }
              }
            } else if (entity != null
                && MediaType.APPLICATION_OCTET_STREAM.equals(entity.getMediaType())) {
              InputStream inputStream = entity.getStream();
              CountingInputStream countingInputStream = new CountingInputStream(inputStream);
              LOG.info("Creating entity blob - octet stream - " + countingInputStream.getCount() + " bytes");
              if (entityRepository.createEntityBlob(
                  appId, namespace, entityType, entityId, blobName, countingInputStream)) {
                pubSubService.updated(appId, namespace, entityType, entityId);
                setStatus(Status.SUCCESS_CREATED);
              } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
              }
            } else {
              badRequest();
            }
          }
        } else {
          setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
      } else {
        return badRequest();
      }
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return null;
  }

  @Override
  public Representation updateBlob(Representation entity) {
    if(replaceAll != null) {
      if(replaceWith == null) {
        return badRequest();
      }
    } else {
      return setBlob(entity);
    }
    Application app = applicationService.read(appId);
    if (app == null) {
      setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      return null;
    }

    String authUserId = null;

    boolean isWriteAccess = false;
    boolean isMaster = false;
    boolean isPublic = false;

    try {
      authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
    } catch (Exception e) {
      // do nothing
    }

    Map<String, Comparable> map =
            entityRepository.getEntity(appId, namespace, entityType, entityId, null);
    List<EntityStub> aclWriteList =
            map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
                    ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE)
                    : new LinkedList<>();

    if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
      isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
    }

    if (isMaster()) {
      isMaster = true;
    } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
      isWriteAccess = true;
    } else if (authUserId != null) {
      List<Role> roles = roleRepository.getRolesOfEntity(appId, namespace, authUserId);
      for (Role role : roles) {
        if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
          isWriteAccess = true;
        }
      }
    }

    if (isMaster || isWriteAccess || isPublic) {
      if(entityRepository.replaceBlobName(appId, namespace, entityType, entityId, replaceAll, replaceWith)) {
        setStatus(Status.SUCCESS_OK);
      } else {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      }
    }
    return null;
  }

  @Override
  public void deleteBlob(Representation entity) {
    try {

      Application app = applicationService.read(appId);
      if (app == null) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return;
      }

      String authUserId = null;

      boolean isWriteAccess = false;
      boolean isMaster = false;
      boolean isPublic = false;

      try {
        authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      } catch (Exception e) {
        // do nothing
      }

      Map<String, Comparable> map =
          entityRepository.getEntity(appId, namespace, entityType, entityId, null);
      List<EntityStub> aclWriteList =
          map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
              ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE)
              : new LinkedList<>();

      if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
        isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
      }

      if (isMaster()) {
        isMaster = true;
      } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
        isWriteAccess = true;
      } else if (authUserId != null) {
        List<Role> roles = roleRepository.getRolesOfEntity(appId, namespace, authUserId);
        for (Role role : roles) {
          if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
            isWriteAccess = true;
          }
        }
      }

      if (isMaster || isWriteAccess || isPublic) {
        if (entityRepository.deleteEntityBlob(appId, namespace, entityType, entityId, blobName)) {
          pubSubService.updated(appId, namespace, entityType, entityId);
          setStatus(Status.SUCCESS_OK);
        } else {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
      } else {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      }

    } catch (Exception e) {
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
  }

  @Override
  public Representation getBlob(Representation entity) {
    try {
      if (!isAuthorized()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }

      Application app = applicationService.read(appId);
      if (app == null) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return null;
      }

      String authUserId = null;

      boolean isWriteAccess = false;
      boolean isMaster = false;
      boolean isPublic = false;

      try {
        authUserId = webTokenService.readUserIdFromToken(app.getMasterKey(), authToken);
      } catch (Exception e) {
        // do nothing
      }

      Map<String, Comparable> map =
          entityRepository.getEntity(appId, namespace, entityType, entityId, null);
      List<EntityStub> aclWriteList =
          map.get(Constants.RESERVED_FIELD_ACL_WRITE) != null
              ? (List<EntityStub>) map.get(Constants.RESERVED_FIELD_ACL_WRITE)
              : new LinkedList<>();

      if (map.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null) {
        isPublic = (boolean) map.get(Constants.RESERVED_FIELD_PUBLICWRITE);
      }

      if (isMaster()) {
        isMaster = true;
      } else if (authUserId != null && ACLHelper.contains(authUserId, aclWriteList)) {
        isWriteAccess = true;
      } else if (authUserId != null) {
        List<Role> roles = roleRepository.getRolesOfEntity(appId, namespace, authUserId);
        for (Role role : roles) {
          if (ACLHelper.contains(role.getEntityId(), aclWriteList)) {
            isWriteAccess = true;
          }
        }
      }


      if (isMaster || isWriteAccess || isPublic) {
        Long count = entityRepository.countEntityBlobSize(appId, namespace, entityType, entityId, blobName);
        InputStream is =
                entityRepository.getEntityBlob(appId, namespace, entityType, entityId, blobName);

        if (encoding != null && encoding.equals("base64")) {
          if (is == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
          } else {
            String base64 = BaseEncoding.base64().encode(ByteStreams.toByteArray(is));
            Representation representation = new StringRepresentation(base64);
            setStatus(Status.SUCCESS_OK);
            return representation;
          }
        } else {
          if (is == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
          } else {
            Representation representation = new InputRepresentation(is);
            representation.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
            representation.setSize(count);
            // representation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT));
            setStatus(Status.SUCCESS_OK);
            return representation;
          }
        }
      } else {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      }

    } catch (Exception e) {
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return null;
  }
}
