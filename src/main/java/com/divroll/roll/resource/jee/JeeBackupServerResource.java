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
package com.divroll.roll.resource.jee;

import com.divroll.roll.model.Application;
import com.divroll.roll.resource.BackupResource;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;
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

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeBackupServerResource extends BaseServerResource
        implements BackupResource {

    private static final String FILE_TO_UPLOAD = "file";

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Override
    public void restore(Representation entity) {
        if (!isAuthorized(appId, apiKey, masterKey)) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return;
        }
        if (entity == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        Application app = applicationService.read(appId);
        if (app == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        if (isMaster(appId, masterKey)) {
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                try {
                    DiskFileItemFactory factory = new DiskFileItemFactory();
                    factory.setSizeThreshold(1000240);
                    RestletFileUpload upload = new RestletFileUpload(factory);
                    FileItemIterator fileIterator = upload.getItemIterator(entity);
                    while (fileIterator.hasNext()) {
                        FileItemStream fi = fileIterator.next();
                        if (fi.getFieldName().equals(FILE_TO_UPLOAD)) {
                            byte[] byteArray = ByteStreams.toByteArray(fi.openStream());
                            // TODO unzip it to folder
                        }
                    }
                } catch (Exception e) {
                    setStatus(Status.SERVER_ERROR_INTERNAL);
                }
            } else {
                setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            }
        } else {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
        return;
    }

    @Override
    public Representation backup(Representation entity) {
        if (!isAuthorized(appId, apiKey, masterKey)) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }
        Application app = applicationService.read(appId);
        if (app == null) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        if (isMaster(appId, masterKey)) {
            try {
                String folderPath = xodusRoot + appId;
                String zipPath = xodusRoot + appId + ".zip";
                ZipUtil.pack(new File(folderPath), new File(zipPath));
                File zipFile = new File(zipPath);
                Representation representation = new FileRepresentation(zipFile, MediaType.APPLICATION_ZIP);
                Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
                disposition.setFilename(appId); // TODO: Not working
                representation.setDisposition(disposition);
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