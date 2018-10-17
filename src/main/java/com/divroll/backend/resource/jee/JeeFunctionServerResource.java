package com.divroll.backend.resource.jee;

import com.divroll.backend.model.Application;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.jee.AppEntityRepository;
import com.divroll.backend.resource.FunctionResource;
import com.divroll.backend.service.ApplicationService;
import com.google.inject.Inject;
import org.mozilla.javascript.*;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.util.Map;

public class JeeFunctionServerResource extends BaseServerResource
    implements FunctionResource {

    private Response response = new Response();

    public class Response {
        private String body;
        public void complete(int code) {
            System.out.println("Completed " + code + " with body - " + body);
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

    @Inject
    ApplicationService applicationService;

    @Inject
    EntityRepository entityRepository;

    @Override
    public Representation getMethod(Representation entity) {
//        AppEntityRepository appEntityRespository = new AppEntityRepository(entityRepository, appId, "User");
//        Object evaluated = eval(appEntityRespository, response,"var id = '1-2'; response.body = 'Hello world'; response.complete(200);");
//        return new StringRepresentation(evaluated.toString());
        return null;
    }

    @Override
    public Representation postMethod(Representation entity) {
        return null;
    }

    @Override
    public Representation putMethod(Representation entity) {
        return null;
    }

    @Override
    public Representation deleteMethod(Representation entity) {
        return null;
    }

}
