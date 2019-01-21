package com.divroll.backend.resource.jee;

import com.divroll.backend.resource.CustomCodesMethodResource;
import org.restlet.representation.Representation;

public class JeeCustomCodesMethodServerResource extends BaseServerResource
    implements CustomCodesMethodResource {
    @Override
    public Representation getJar(Representation entity) {
        return null;
    }

    @Override
    public Representation createJar(Representation entity) {
        return null;
    }

    @Override
    public Representation updateJar(Representation entity) {
        return createJar(entity);
    }

    @Override
    public Representation deleteJar(Representation entity) {
        return null;
    }

    @Override
    public void optionsMethod(Representation entity) {

    }
}
