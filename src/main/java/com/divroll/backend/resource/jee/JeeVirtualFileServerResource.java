package com.divroll.backend.resource.jee;

import com.divroll.backend.model.File;
import com.divroll.backend.resource.VirtualFileResource;
import org.restlet.representation.Representation;

public class JeeVirtualFileServerResource extends BaseServerResource
    implements VirtualFileResource {
    @Override
    public File createVirtualFile(Representation entity) {
        return null;
    }

    @Override
    public void deleteVirtualFile(Representation entity) {

    }

    @Override
    public Representation getVirtualFile(Representation entity) {
        return null;
    }
}
