package com.divroll.backend.resource.jee;

import com.divroll.backend.model.Application;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.resource.FunctionResource;
import com.divroll.backend.service.ApplicationService;
import com.google.inject.Inject;
import org.dynjs.Config;
import org.dynjs.runtime.DynJS;
import org.dynjs.runtime.Runner;
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

    public class ScopedEntity {

        EntityRepository repository;
        String instance;
        String storeName;

        public ScopedEntity(EntityRepository repository, String instance, String storeName) {
            this.repository = repository;
            this.storeName = storeName;
            this.instance = instance;
        }

        public Map<String,Object> getEntityById(String entityId) {
            return repository.getEntity(instance, storeName, entityId);
        }

    }

    @Override
    public Representation getMethod(Representation entity) {

//        Config config = new Config();
//        DynJS dynjs = new DynJS(config);
//        Runner runner = dynjs.newRunner();
//        Object result = runner.withSource("var x = 100; console.log(x);");
//
        Application application = applicationService.read(appId);

        ScopedEntity scopedEntity = new ScopedEntity(entityRepository, appId, "User");
        Object evaluated = eval(scopedEntity, response,"var id = '1-2'; response.body = 'Hello world'; response.complete(200);");
//        Object evaluated = eval(scopedEntity, response,"var id = '1-2'; response.body = 'Hello world'; response.complete(200); return this.getEntityById(id)");
        return new StringRepresentation(evaluated.toString());
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

    private Object eval(Object obj, Response response, String expression) {
        Context cx = Context.enter();
        try {
            ScriptableObject scope = cx.initStandardObjects();

            // convert my "this" instance to JavaScript object
            Object jsObj = Context.javaToJS(obj, scope);
            Object jsRespObj = Context.javaToJS(response, scope);

            ScriptableObject.putProperty(scope, "response", jsRespObj);

            // prepare envelope function run()
            cx.evaluateString(scope,
                    String.format("function onRequest() { %s return response; } ", expression),
                    "<func>", 1, null);

            // call method run()
            Object fObj = scope.get("onRequest", scope);
            Function f = (Function) fObj;
            Object result = f.call(cx, scope, (Scriptable) jsObj, null);
            if (result instanceof Wrapper)
                return ((Wrapper) result).unwrap();
            return result;

        } finally {
            Context.exit();
        }
    }
}
