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
package com.divroll.backend.resource.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.File;
import com.divroll.backend.repository.FileStore;
import com.divroll.backend.resource.FileResource;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.CountingInputStream;
import com.google.inject.Inject;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeFileServerResource extends BaseServerResource implements FileResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeFileServerResource.class);

  @Inject FileStore fileStore;

  @Override
  public File createFile(Representation entity) {

    if(destinationFile == null || destinationFile.isEmpty()) {
      JSONObject jsonApiArg = new JSONObject(apiArg);
      destinationFile = jsonApiArg.getString(Constants.RESERVED_DESTINATION_FILE);
    }
    if(destinationFile == null || destinationFile.isEmpty()) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return null;
    }

    try {
      if (!isSuperUser()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }
      if (appId == null || appId.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
      }
      if (entity != null && MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
        Request restletRequest = getRequest();
        HttpServletRequest servletRequest = ServletUtils.getRequest(restletRequest);
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator fileIterator = upload.getItemIterator(servletRequest);
        while (fileIterator.hasNext()) {
          FileItemStream item = fileIterator.next();
          if (item.isFormField()) {
          } else {
            CountingInputStream countingInputStream = new CountingInputStream(item.openStream());
            File file = fileStore.put(appId, namespace, destinationFile, countingInputStream);
            long count = countingInputStream.getCount();
            LOG.with(file).info("File size=" + count);
            setStatus(Status.SUCCESS_CREATED);
            return file;
          }
        }
      } else if (entity != null
              && MediaType.APPLICATION_OCTET_STREAM.equals(entity.getMediaType())) {
        InputStream inputStream = entity.getStream();
        CountingInputStream countingInputStream = new CountingInputStream(inputStream);
        File file = fileStore.put(appId, namespace, destinationFile, countingInputStream);
        long count = countingInputStream.getCount();
        LOG.with(file).info("File size=" + count);
        setStatus(Status.SUCCESS_CREATED);
        return file;
      } else {
        badRequest();
      }
    } catch (Exception e) {
      e.printStackTrace();
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    return null;
  }

  @Override
  public void deleteFile(Representation entity) {
    try {

      if (!isSuperUser()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return;
      }
      if(destinationFile == null || destinationFile.isEmpty()) {
        try {
          JSONObject jsonApiArg = new JSONObject(apiArg);
          destinationFile = jsonApiArg.getString(Constants.RESERVED_DESTINATION_FILE);
        } catch (Exception e) {
        }
      }

      if(destinationFile == null || destinationFile.isEmpty()) {
        if (appId == null || appId.isEmpty()) {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          return;
        }
        boolean deleted = fileStore.deleteAll(appId);
        if (deleted) {
          setStatus(Status.SUCCESS_OK);
        } else {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
      } else {
        if (appId == null || appId.isEmpty()) {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          return;
        }
        boolean deleted = fileStore.delete(appId, namespace, destinationFile);
        if (deleted) {
          setStatus(Status.SUCCESS_OK);
        } else {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
      }


    } catch (Exception e) {
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
  }

  @Override
  public Representation getFile(Representation entity) {
    try {

      String filePath = getQueryValue("filePath");
      if(filePath != null) {
        InputStream is = fileStore.getStream(appId, namespace, filePath);
        if(is != null){
          Representation representation = new InputRepresentation(is);
          representation.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
          setStatus(Status.SUCCESS_OK);
          return representation;
        } else {
          setStatus(Status.CLIENT_ERROR_NOT_FOUND);
          return null;
        }
      }

      String fileId = getAttribute("fileId");
      if(fileId != null) {
        Map<String,Object> map = webTokenService.readToken(masterSecret, fileId);
        Long id = (Long) map.get(Constants.JWT_ID_KEY);
        if(id != null) {
          InputStream is =fileStore.getStream(appId, id);
          if(is != null){
            Representation representation = new InputRepresentation(is);
            representation.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
            setStatus(Status.SUCCESS_OK);
            return representation;
          } else {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
          }
        }
      }

      if (!isSuperUser()) {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        return null;
      }

      if(apiArg != null && !apiArg.isEmpty()) {
        if(sourceFile == null || sourceFile.isEmpty() && apiArg != null && !apiArg.isEmpty()) {
          JSONObject jsonApiArg = new JSONObject(apiArg);
          sourceFile = jsonApiArg.getString(Constants.RESERVED_SOURCE_FILE);
        }
        if(sourceFile == null || sourceFile.isEmpty()) {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          return null;
        }
        InputStream is = fileStore.getStream(appId, namespace, sourceFile);
        if(is != null){
          Representation representation = new InputRepresentation(is);
          representation.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
          // representation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT));
          setStatus(Status.SUCCESS_OK);
          return representation;
        } else {
          setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
      } else {
        // List files
        JSONObject response = new JSONObject();
        JSONArray files = new JSONArray();
        fileStore.list(appId).forEach(file -> {
          JSONObject fileJSONObject = new JSONObject();
          fileJSONObject.put("path", file.getName());
          String fileToken = webTokenService.createToken(masterSecret, file.getDescriptor());
          fileJSONObject.put("fileId", file.getDescriptor());
          fileJSONObject.put("created", file.getCreated());
          fileJSONObject.put("lastModified", file.getModified());
          files.put(fileJSONObject);
        });
        response.put("files", files);
        String jsonString = response.toString();
        Representation representation = new JsonRepresentation(jsonString);
        representation.setMediaType(MediaType.APPLICATION_JSON);
        setStatus(Status.SUCCESS_OK);
        return representation;
      }

      if(apiArg == null || apiArg.isEmpty()) {
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
      }


    } catch (Exception e) {
      setStatus(Status.SERVER_ERROR_INTERNAL);
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Representation updateFile(Representation entity) {
    String operation = null;

    if (!isSuperUser()) {
      setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      return null;
    }

    if(apiArg == null || apiArg.isEmpty()) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return null;
    }

    if( (destinationFile == null || destinationFile.isEmpty())  ||
            (sourceFile == null || sourceFile.isEmpty())) {
      JSONObject jsonApiArg = new JSONObject(apiArg);
      destinationFile = jsonApiArg.getString(Constants.RESERVED_DESTINATION_FILE);
      sourceFile = jsonApiArg.getString(Constants.RESERVED_SOURCE_FILE);
      operation = jsonApiArg.getString(Constants.RESERVED_OPERATION);
    }

    if( (destinationFile == null || destinationFile.isEmpty())  ||
            (sourceFile == null || sourceFile.isEmpty())) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return null;
    }

    if(operation == null || operation.isEmpty()) {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return null;
    }

    if(operation.equals(Constants.RESERVED_OPERATION_MOVE)) {
      if(fileStore.move(appId, namespace, sourceFile, destinationFile)) {
        setStatus(Status.SUCCESS_ACCEPTED);
        return null;
      }
    } else {
      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
      return null;
    }

    return null;
  }
}
