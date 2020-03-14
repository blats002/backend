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

import com.divroll.backend.model.Application;
import com.divroll.backend.resource.BackupResource;
import com.divroll.backend.xodus.XodusManager;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.util.CompressBackupUtil;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.InputStream;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeBackupServerResource extends BaseServerResource implements BackupResource {

  private static final Logger LOG = LoggerFactory.getLogger(JeeBackupServerResource.class);
  private static final String FILE_TO_UPLOAD = "file";

  @Inject
  @Named("xodusRoot")
  String xodusRoot;

  @Inject
  XodusManager manager;

  @Override
  public void restore(Representation entity) {
    try {
      if (isMaster()) {
        if (entity == null) {
          setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
          return;
        }
        Application app = applicationService.read(appId);
        if (app == null) {
          setStatus(Status.CLIENT_ERROR_NOT_FOUND);
          return;
        }
        if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
          try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1000240);
            RestletFileUpload upload = new RestletFileUpload(factory);
            FileItemIterator fileIterator = upload.getItemIterator(entity);
            while (fileIterator.hasNext()) {
              FileItemStream fi = fileIterator.next();
              if (fi.getFieldName().equals(FILE_TO_UPLOAD)) {
                CountingInputStream countingInputStream = new CountingInputStream(fi.openStream());
                LOG.info("Processing backup upload - octet stream - " + countingInputStream.getCount() + " bytes");
                PersistentEntityStore store = manager.getPersistentEntityStore(xodusRoot, appId);
                ZipUtil.unpack(countingInputStream, new File(store.getLocation()));
              }
            }
            setStatus(Status.SUCCESS_ACCEPTED);
          } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
          }
        } else if(entity != null
                && MediaType.APPLICATION_OCTET_STREAM.equals(entity.getMediaType())) {
          InputStream inputStream = entity.getStream();
          CountingInputStream countingInputStream = new CountingInputStream(inputStream);
          LOG.info("Processing backup upload - octet stream - " + countingInputStream.getCount() + " bytes");
          PersistentEntityStore store = manager.getPersistentEntityStore(xodusRoot, appId);
          ZipUtil.unpack(countingInputStream, new File(store.getLocation()));
          setStatus(Status.SUCCESS_ACCEPTED);
        } else {
          setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
        }
      } else {
        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
      }
    } catch (Exception e) {
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
  }

  @Override
  public Representation backup(Representation entity) {
    if (isMaster()) {
      Application app = applicationService.read(appId);
      if (app == null) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return null;
      }
      try {

        PersistentEntityStore store = manager.getPersistentEntityStore(xodusRoot, appId);
        final File backupFile = CompressBackupUtil.backup(store, new File(store.getLocation(), "backups"), null, true);

        Representation representation = new FileRepresentation(backupFile, MediaType.APPLICATION_ZIP);
        Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
        disposition.setFilename(backupFile.getName());
        representation.setDisposition(disposition);
        setStatus(Status.SUCCESS_OK);
        return representation;
      } catch (Exception e) {
        e.printStackTrace();
        setStatus(Status.SERVER_ERROR_INTERNAL);
      }
    } else {
      setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    return null;
  }
}
