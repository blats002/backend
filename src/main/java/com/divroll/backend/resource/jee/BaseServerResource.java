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
package com.divroll.backend.resource.jee;

import com.alibaba.fastjson.JSONObject;
import com.divroll.backend.Constants;
import com.divroll.backend.guice.SelfInjectingServerResource;
import com.divroll.backend.job.EmailJob;
import com.divroll.backend.job.RetryJobWrapper;
import com.divroll.backend.model.*;
import com.divroll.backend.model.filter.TransactionFilter;
import com.divroll.backend.model.filter.TransactionFilterParser;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.jee.AppEntityRepository;
import com.divroll.backend.service.ApplicationService;
import com.divroll.backend.service.SchemaService;
import com.divroll.backend.service.jee.AppEmailService;
import com.divroll.backend.trigger.TriggerRequest;
import com.divroll.backend.trigger.TriggerResponse;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.*;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;
import scala.actors.threadpool.Arrays;

import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class BaseServerResource extends SelfInjectingServerResource {

    protected static final Integer DEFAULT_LIMIT = 100;
    private static final Logger LOG
            = LoggerFactory.getLogger(BaseServerResource.class);
    protected Map<String, Object> queryMap = new LinkedHashMap<>();
    protected Map<String, String> propsMap = new LinkedHashMap<>();

    protected String appName;
    protected String entityId;
    protected String entityType;
    protected String blobName;
    protected String appId;
    protected String apiKey;
    protected String masterKey;
    protected String authToken;
    protected String aclRead;
    protected String aclWrite;
    protected String accept;
    protected String contentType;
    protected String userId;
    protected String username;
    protected String roleId;
    protected Integer skip = null;
    protected Integer limit = null;
    protected String sort;
    protected Boolean publicRead;
    protected Boolean publicWrite;

    protected String propertyName;

    protected String linkName;
    protected String targetEntityId;

    protected String masterToken;

    protected List<TransactionFilter> filters;

    private Application application;

    protected String fileName;

    @Inject
    ApplicationService applicationService;

    @Inject
    EntityRepository entityRepository;

    @Inject
    SchemaService schemaService;

    @Override
    protected void doInit() {
        super.doInit();
        filters = new LinkedList<>();
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series(Header.class);
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }
        responseHeaders.add(new Header("X-Powered-By", Constants.SERVER_NAME));
        setAllowedMethods(Sets.newHashSet(Method.GET,
                Method.PUT,
                Method.POST,
                Method.DELETE,
                Method.OPTIONS));
        propsMap = appProperties();
        entityId = getAttribute(Constants.RESERVED_FIELD_ENTITY_ID);
        entityType = getAttribute(Constants.ENTITY_TYPE);
        blobName = getAttribute("blobName");
        userId = getAttribute(Constants.USER_ID);
        roleId = getAttribute(Constants.ROLE_ID);
        propertyName = getAttribute("propertyName");
        linkName = getAttribute("linkName");
        targetEntityId = getAttribute("targetEntityId");

        username = getQueryValue(Constants.QUERY_USERNAME);
        sort = getQueryValue("sort");

        Series headers = (Series) getRequestAttributes().get("org.restlet.http.headers");

        LOG.with(headers).info("Logging headers");

        appId = headers.getFirstValue(Constants.HEADER_APP_ID) != null
                ? headers.getFirstValue(Constants.HEADER_APP_ID) : headers.getFirstValue(Constants.HEADER_APP_ID.toLowerCase());
        apiKey = headers.getFirstValue(Constants.HEADER_API_KEY) != null
                ? headers.getFirstValue(Constants.HEADER_API_KEY) : headers.getFirstValue(Constants.HEADER_API_KEY.toLowerCase());
        masterKey = headers.getFirstValue(Constants.HEADER_MASTER_KEY) != null
                ? headers.getFirstValue(Constants.HEADER_MASTER_KEY) : headers.getFirstValue(Constants.HEADER_MASTER_KEY.toLowerCase());
        authToken = headers.getFirstValue(Constants.HEADER_AUTH_TOKEN) != null
                ? headers.getFirstValue(Constants.HEADER_AUTH_TOKEN) : headers.getFirstValue(Constants.HEADER_AUTH_TOKEN.toLowerCase());
        masterToken = headers.getFirstValue(Constants.HEADER_MASTER_TOKEN) != null
                ? headers.getFirstValue(Constants.HEADER_MASTER_TOKEN) : headers.getFirstValue(Constants.HEADER_MASTER_TOKEN.toLowerCase());

        if(appId == null) {
            appId = getQueryValue(Constants.APP_ID);
        }

        if(apiKey == null) {
            apiKey= getQueryValue(Constants.API_KEY);
        }

        if(masterKey == null) {
            masterKey= getQueryValue(Constants.MASTER_KEY);
        }

        aclRead = headers.getFirstValue(Constants.HEADER_ACL_READ) != null
                ? headers.getFirstValue(Constants.HEADER_ACL_READ) : headers.getFirstValue(Constants.HEADER_ACL_READ.toLowerCase());
        aclWrite = headers.getFirstValue(Constants.HEADER_ACL_WRITE) != null
                ? headers.getFirstValue(Constants.HEADER_ACL_WRITE) : headers.getFirstValue(Constants.HEADER_ACL_WRITE.toLowerCase());
        accept = headers.getFirstValue(Constants.HEADER_ACCEPT) != null
                ? headers.getFirstValue(Constants.HEADER_ACCEPT) : headers.getFirstValue(Constants.HEADER_ACCEPT.toLowerCase());
        contentType = headers.getFirstValue(Constants.HEADER_CONTENT_TYPE) != null
                ? headers.getFirstValue(Constants.HEADER_CONTENT_TYPE) : headers.getFirstValue(Constants.HEADER_CONTENT_TYPE.toLowerCase());

        appName = getAttribute("appName");

        try {
            skip = Integer.valueOf(getQueryValue(Constants.QUERY_SKIP));
            limit = Integer.valueOf(getQueryValue(Constants.QUERY_LIMIT));
        } catch (Exception e) {
            // do nothing
        }

        if(skip == null) {
            skip = 0;
        }

        if(limit == null) {
            limit = DEFAULT_LIMIT;
        }

        try {
            publicRead = Boolean.valueOf(getQueryValue("publicRead"));
        } catch (Exception e) {

        }

        try {
            publicWrite = Boolean.valueOf(getQueryValue("publicWrite"));
        } catch (Exception e) {

        }

        if(appId != null) {
            application = applicationService.read(appId);
        }

        String queries = getQueryValue("queries");
        if(queries != null) {
            try {
                filters = new TransactionFilterParser().parseQuery(queries);
            } catch (Exception e) {

            }
        }

        fileName = getAttribute("fileName");

    }

    protected Map<String, String> appProperties() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        InputStream is = getContext().getClass().getResourceAsStream("/app.properties");
        Properties props = new Properties();
        try {
            props.load(is);
            map = new LinkedHashMap<String, String>((Map) props);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    protected byte[] toByteArray(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    protected Object fromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }

    protected boolean isAuthorized() {
        if (application != null) {
            if (BCrypt.checkpw(masterKey, application.getMasterKey())) {
                return true;
            }
            if (BCrypt.checkpw(apiKey, application.getApiKey())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isMaster() {
        if (application != null && (BCrypt.checkpw(masterKey, application.getMasterKey()))) {
            return true;
        }
        return false;
    }


    protected Representation returnNull() {
        JSONObject jsonObject = new JSONObject();
        return new JsonRepresentation(jsonObject.toJSONString());
    }

    protected Representation returnServerError() {
        JSONObject jsonObject = new JSONObject();
        setStatus(Status.SERVER_ERROR_INTERNAL);
        return new JsonRepresentation(jsonObject.toJSONString());
    }

    protected Representation returnMissingAuthToken() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", Constants.ERROR_MISSING_AUTH_TOKEN);
        return new JsonRepresentation(jsonObject.toJSONString());
    }

    protected Representation missingUsernamePasswordPair() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", Constants.ERROR_MISSING_USERNAME_PASSWORD);
        return new JsonRepresentation(jsonObject.toJSONString());
    }

    protected Map<String, Object> cleanup(Map<String, Object> result) {
        result.remove(Constants.RESERVED_FIELD_PUBLICWRITE);
        result.remove(Constants.RESERVED_FIELD_PUBLICREAD);
        return result;
    }

    protected boolean validateId(String id) {
        return (id != null && !id.isEmpty() && !id.equalsIgnoreCase("null"));
    }

    protected void validateIds(String[] read, String[] write) throws IllegalArgumentException {
        if(read != null) {
            List<String> idList =  Arrays.asList(read);
            for(String id : idList) {
                if(id == null) {
                    throw new IllegalArgumentException();
                }
                if(id.isEmpty()) {
                    throw new IllegalArgumentException();
                }
            }
        }
        if(write != null) {
            List<String> idList =  Arrays.asList(write);
            for(String id : idList) {
                if(id == null) {
                    throw new IllegalArgumentException();
                }
                if(id.isEmpty()) {
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    protected Application getApp() {
        return application;
    }

    protected void validateSchema(String entityType, Map<String,Comparable> comparableMap)
            throws IllegalArgumentException {
        List<EntityType> entityTypes = schemaService.listSchemas(appId);
        entityTypes.forEach(type -> {
            if(type.getEntityType().equalsIgnoreCase("users")) {
                List<EntityPropertyType> propertyTypes = type.getPropertyTypes();
                propertyTypes.forEach(propertyType -> {
                    propertyType.getPropertyName();
                });

            } else if(type.getEntityType().equalsIgnoreCase("roles")) {
                List<EntityPropertyType> propertyTypes = type.getPropertyTypes();

            } else if(type.getEntityType().equalsIgnoreCase(entityType)) {
                List<EntityPropertyType> propertyTypes = type.getPropertyTypes();

            }
        });
        comparableMap.forEach((key,value) -> {
            List<EntityPropertyType> types = schemaService.listPropertyTypes(appId, entityType);
            types.forEach(type -> {
                if(type.equals(key)) {
                    EntityPropertyType.TYPE expectedPropertyType = type.getPropertyType();
                    if(value instanceof EmbeddedEntityIterable) {
                        if(!expectedPropertyType.equals(EntityPropertyType.TYPE.OBJECT)) {
                            throw new IllegalArgumentException("Property " + key + " should be a " + type.toString());
                        }
                    } else if(value instanceof EmbeddedArrayIterable) {
                        if(!expectedPropertyType.equals(EntityPropertyType.TYPE.ARRAY)) {
                            throw new IllegalArgumentException("Property " + key + " should be a " + type.toString());
                        }
                    } else if(value instanceof Boolean) {
                        if(!expectedPropertyType.equals(EntityPropertyType.TYPE.BOOLEAN)) {
                            throw new IllegalArgumentException("Property " + key + " should be a " + type.toString());
                        }
                    } else if(value instanceof String) {
                        if(!expectedPropertyType.equals(EntityPropertyType.TYPE.STRING)) {
                            throw new IllegalArgumentException("Property " + key + " should be a " + type.toString());
                        }
                    } else if(value instanceof Number) {
                        if(!expectedPropertyType.equals(EntityPropertyType.TYPE.NUMBER)) {
                            throw new IllegalArgumentException("Property " + key + " should be a " + type.toString());
                        }
                    }
                }
            });

        });
    }

    public boolean beforeSave(Map<String, Comparable> entity, String appId, String entityType, TriggerResponse response, String expression) {
        AppEntityRepository repository = new AppEntityRepository(entityRepository, appId, entityType);
        AppEmailService emailService = null;
        Application application = getApp();
        if(application != null && application.getEmailConfig() != null) {
            Email emailConfig = application.getEmailConfig();
            emailService = new AppEmailService(emailConfig);
        }
        TriggerRequest request = new TriggerRequest(entity, entityType, repository, emailService);
        Object evaluated = eval(request, response, expression);
        response = (TriggerResponse) evaluated;
        LOG.info("Evaluated: " + String.valueOf(((TriggerResponse) evaluated).isSuccess()));
        LOG.info("Response Body: " + String.valueOf(((TriggerResponse) evaluated).getBody()));
        return response.isSuccess();
    }

    public boolean afterSave(Map<String, Comparable> entity, String appId, String entityType, TriggerResponse response, String expression) {
        if(expression != null && !expression.isEmpty()) {
            AppEntityRepository repository = new AppEntityRepository(entityRepository, appId, entityType);
            AppEmailService emailService = null;
            Application application = getApp();
            if(application != null && application.getEmailConfig() != null) {
                Email emailConfig = application.getEmailConfig();
                emailService = new AppEmailService(emailConfig);
            }
            TriggerRequest request = new TriggerRequest(entity, entityType, repository, emailService);
            Object evaluated = eval(request, response, expression);
            response = (TriggerResponse) evaluated;
            LOG.info("Evaluated: " + String.valueOf(((TriggerResponse) evaluated).isSuccess()));
            LOG.info("Response Body: " + String.valueOf(((TriggerResponse) evaluated).getBody()));
            return response.isSuccess();
        } else {
            return true;
        }

    }

    protected Object eval(TriggerRequest request, TriggerResponse response, String expression) {
        Context cx = Context.enter();
        try {
            ScriptableObject scope = cx.initStandardObjects();

            // convert my "this" instance to JavaScript object
            Object jsReqObj = Context.javaToJS(request, scope);
            Object jsRespObj = Context.javaToJS(response, scope);

            // Convert it to a NativeObject (yes, this could have been done directly)
            NativeObject nobj = new NativeObject();
            for (Map.Entry<String, Comparable> entry : request.getEntity().entrySet()) {
                nobj.defineProperty(entry.getKey(), entry.getValue(), NativeObject.READONLY);
            }

            ScriptableObject.putProperty(scope, "response", jsRespObj);
            ScriptableObject.putProperty(scope, "request", jsReqObj);
            ScriptableObject.putProperty(scope, "entity", nobj);

            // prepare envelope function run()
            cx.evaluateString(scope,
                    String.format("function onRequest() { %s return response; } ", expression),
                    "<func>", 1, null);

            // call method run()
            Object fObj = scope.get("onRequest", scope);
            Function f = (Function) fObj;
            Object result = f.call(cx, scope, (Scriptable) jsReqObj, null);
            if (result instanceof Wrapper)
                return ((Wrapper) result).unwrap();
            return result;

        } finally {
            Context.exit();
        }
    }

    protected List<JsFunction> parseJS(String jsCode) {
        List<JsFunction> jsFunctions = new LinkedList<>();
        AstRoot astRoot = new Parser().parse(jsCode, null, 1);
        List<AstNode> statList = astRoot.getStatements();

        Map<String,String> functionBodyMap = new HashMap<>();

        for(Iterator<AstNode> iter = statList.iterator(); iter.hasNext();) {
            AstNode astNode = iter.next();
            if(astNode.getType() == Token.FUNCTION) {
                FunctionNode fNode = (FunctionNode) astNode;
                System.out.println("*** function Name : " + fNode.getName() + ", paramCount : " + fNode.getParams() + ", depth : " + fNode.depth());
                AstNode bNode = fNode.getBody();
                Block block = (Block)bNode;
                String source = block.toSource();
                System.out.println("JS Source : " + source);
                functionBodyMap.put(fNode.getName(), source);

                JsFunction jsFunction = new JsFunction();
                jsFunction.setFunctionName(fNode.getName());
                jsFunction.setExpression(source);
                jsFunctions.add(jsFunction);

            }
        }

//        for(Iterator<AstNode> iter = statList.iterator(); iter.hasNext();) {
//            AstNode astNode = iter.next();
//            if(astNode.getType() == Token.EXPR_RESULT) {
//                ExpressionStatement expressionStatement = (ExpressionStatement) astNode;
//                FunctionCall fCallNode = (FunctionCall) expressionStatement.getExpression();
//                Name nameNode = (Name) fCallNode.getTarget();
//                AstNode arg = Iterables.getFirst(fCallNode.getArguments(), null);
//                System.out.println("*** function Name : " + nameNode.getIdentifier());
//                System.out.print("*** function Call : " + fCallNode.getArguments());
//                if(arg != null) {
//                    StringLiteral stringLiteral = (StringLiteral) arg;
//                    String entityType = stringLiteral.getValue();
//                    System.out.print("*** entity Type : " + entityType);
//                    JsFunction jsFunction = new JsFunction();
//                    jsFunction.setFunctionName(nameNode.getIdentifier());
//                    jsFunction.setArguments(Arrays.asList(new String[]{entityType}));
//                    jsFunction.setExpression(functionBodyMap.get(nameNode.getIdentifier()));
//                    jsFunctions.add(jsFunction);
//                }
//            }
//        }

        return jsFunctions;
    }

}
