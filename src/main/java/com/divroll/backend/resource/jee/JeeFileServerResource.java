package com.divroll.backend.resource.jee;

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
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

public class JeeFileServerResource extends BaseServerResource implements FileResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeFileServerResource.class);

    @Inject
    FileStore fileStore;

    @Override
    public File createFile(Representation entity) {
        try {
            if(!isAuthorized()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if (entity != null && MediaType.MULTIPART_FORM_DATA.equals(
                    entity.getMediaType(), true)) {
                Request restletRequest = getRequest();
                HttpServletRequest servletRequest = ServletUtils.getRequest(restletRequest);
                ServletFileUpload upload = new ServletFileUpload();
                FileItemIterator fileIterator = upload.getItemIterator(servletRequest);
                while (fileIterator.hasNext()) {
                    FileItemStream item = fileIterator.next();
                    String fieldName = item.getFieldName();
                    String name = item.getName();
                    if(item.isFormField()) {
                    } else {
                        CountingInputStream countingInputStream = new CountingInputStream(item.openStream());
                        File file = fileStore.put(name, countingInputStream);
                        long count = countingInputStream.getCount();
                        LOG.with(file).info("File size=" + count);
                        setStatus(Status.SUCCESS_CREATED);
                        return file;
                    }
                }
            }
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public void deleteFile(Representation entity) {
        try {
            if(!isMaster()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            boolean deleted = fileStore.delete(fileName);
            if(deleted) {
                setStatus(Status.SUCCESS_OK);
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public Representation getFile(Representation entity) {
        try {
            if(fileName == null || fileName.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }
            InputStream is = fileStore.getStream(fileName);
            Representation representation = new InputRepresentation(is);
            representation.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
            //representation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT));
            setStatus(Status.SUCCESS_OK);
            return representation;
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }
}
