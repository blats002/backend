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
package com.divroll.backend.service.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.helper.ACLHelper;
import com.divroll.backend.helper.EntityIterables;
import com.divroll.backend.helper.ObjectLogger;
import com.divroll.backend.model.*;
import com.divroll.backend.model.action.Action;
import com.divroll.backend.model.action.EntityAction;
import com.divroll.backend.model.builder.*;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.repository.jee.AppEntityRepository;
import com.divroll.backend.service.EntityService;
import com.divroll.backend.service.PubSubService;
import com.divroll.backend.service.SchemaService;
import com.divroll.backend.trigger.TriggerRequest;
import com.divroll.backend.trigger.TriggerResponse;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.javascript.*;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.FunctionNode;
import scala.actors.threadpool.Arrays;
import util.ComparableLinkedList;

import java.io.InputStream;
import java.util.*;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeEntityService implements EntityService {

  private static final Logger LOG = LoggerFactory.getLogger(JeeEntityService.class);

  @Inject
  @Named("defaultFunctionStore")
  String defaultFunctionStore;

  @Inject
  @Named("defaultUserStore")
  String defaultUserStore;

  @Inject
  @Named("defaultRoleStore")
  String defaultRoleStore;

  @Inject EntityRepository entityRepository;

  @Inject SchemaService schemaService;

  @Inject UserRepository userRepository;

  @Inject RoleRepository roleRepository;

  @Inject PubSubService pubSubService;

    @Override
    public EntityACL retrieveEntityACLWriteList(Application application,
                                                   String namespace,
                                                   String entityType,
                                                   String propertyName,
                                                   Comparable propertyValue) {
      LOG.info("nameSpace=" + namespace);
      LOG.info("entityType=" + entityType);
      LOG.info("propertyName=" + propertyName);
      LOG.info("propertyValue=" + propertyValue);
        List<Map<String,Comparable>> comparableMaps = entityRepository.getEntities(application.getAppId(), namespace, entityType, propertyName, propertyValue);
        ObjectLogger.log(comparableMaps);

        final List<String> aclWriteListResult = new LinkedList<>();
        final List<String> aclReadListResult = new LinkedList<>();
        Boolean publicRead = null;
        Boolean publicWrite = null;

        if(comparableMaps != null && !comparableMaps.isEmpty()) {
            if(comparableMaps.size() == 1) {
                Map comparableMap = comparableMaps.iterator().next();

                // aclWrite
                ComparableLinkedList<Comparable> aclWriteList
                        = (ComparableLinkedList<Comparable>) comparableMap.get(Constants.RESERVED_FIELD_ACL_WRITE);
                aclWriteList.forEach(aclWrite -> {
                    EntityStub entityStub = (EntityStub) aclWrite;
                    aclWriteListResult.add(entityStub.getEntityId());
                });

                // aclRead
                ComparableLinkedList<Comparable> aclReadList
                      = (ComparableLinkedList<Comparable>) comparableMap.get(Constants.RESERVED_FIELD_ACL_READ);
                aclReadList.forEach(aclWrite -> {
                  EntityStub entityStub = (EntityStub) aclWrite;
                  aclReadListResult.add(entityStub.getEntityId());
                });

                publicWrite =
                        comparableMap.get(Constants.RESERVED_FIELD_PUBLICWRITE) != null
                                ? (Boolean) comparableMap.get(Constants.RESERVED_FIELD_PUBLICWRITE)
                                : null;
                publicRead = comparableMap.get(Constants.RESERVED_FIELD_PUBLICREAD) != null
                        ? (Boolean) comparableMap.get(Constants.RESERVED_FIELD_PUBLICREAD)
                        : null;

                return new EntityACLBuilder()
                        .read(aclReadListResult)
                        .write(aclWriteListResult)
                        .publicRead(publicRead)
                        .publicWrite(publicWrite)
                        .build();
            } else {
              throw new IllegalArgumentException("Multiple entities found error");
            }
        }
        // no entities found
        return null;
    }

    @Override
  public JSONObject createEntity(
      Application application,
      String namespace,
      String entityType,
      Map<String, Comparable> comparableMap,
      String aclRead,
      String aclWrite,
      Boolean publicRead,
      Boolean publicWrite,
      List<Action> actions,
      List<EntityAction> entityActions,
      CreateOption createOption,
      EntityMetadata metadata)
      throws Exception {
    return createEntity(
        application,
        namespace,
        entityType,
        comparableMap,
        aclRead,
        aclWrite,
        publicRead,
        publicWrite,
        actions,
        entityActions,
        createOption,
        null,
        null,
        metadata);
  }

  @Override
  public JSONObject createEntity(
      Application application,
      String namespace,
      String entityType,
      Map<String, Comparable> comparableMap,
      String aclRead,
      String aclWrite,
      Boolean publicRead,
      Boolean publicWrite,
      List<Action> actions,
      List<EntityAction> entityActions,
      CreateOption createOption,
      String blobName,
      InputStream blobStream,
      EntityMetadata metadata)
      throws Exception {

    LOG.with("application", application);
    LOG.with("namespace", namespace);
    LOG.with("entityType", entityType);
    LOG.with("comparableMap", comparableMap);
    LOG.with("aclRead", aclRead);
    LOG.with("aclWrite", aclWrite);
    LOG.with("publicRead", publicRead);
    LOG.with("publicWrite", publicWrite);
    LOG.with("publicWrite", publicWrite);
    LOG.with("actions", actions);
    LOG.with("entityActions", entityActions);
    LOG.with("createOption", createOption);
    LOG.with("blobName", blobName);
    LOG.with("blobStream", blobStream);
    LOG.with("metadata", metadata);

    JSONObject result = new JSONObject();

    String[] read = new String[] {};
    String[] write = new String[] {};

    if (aclRead != null) {
      try {
        JSONArray jsonArray = new JSONArray(aclRead);
        read = ACLHelper.onlyIds(jsonArray);
      } catch (Exception e) {
        // do nothing
      }
    }

    if (aclWrite != null) {
      try {
        JSONArray jsonArray = new JSONArray(aclWrite);
        write = ACLHelper.onlyIds(jsonArray);
      } catch (Exception e) {
        // do nothing
      }
    }

    ObjectLogger.log(comparableMap);
    if (!comparableMap.isEmpty()) {

      if (comparableMap.get("publicRead") != null) {
        Comparable publicReadComparable = comparableMap.get("publicRead");
        if (publicReadComparable instanceof Boolean) {
          publicRead = (Boolean) publicReadComparable;
        } else if (publicReadComparable instanceof String) {
          publicRead = Boolean.valueOf((String) publicReadComparable);
        }
      }

      if (comparableMap.get("publicWrite") != null) {
        Comparable publicWriteComparable = comparableMap.get("publicWrite");
        if (publicWriteComparable instanceof Boolean) {
          publicWrite = (Boolean) publicWriteComparable;
        } else if (publicWriteComparable instanceof String) {
          publicWrite = Boolean.valueOf((String) publicWriteComparable);
        }
      }

      // System.out.println("READ: " + ((EmbeddedArrayIterable)
      // comparableMap.get(Constants.RESERVED_FIELD_ACL_READ)).asJSONArray());
      // System.out.println("WRITE: " + ((EmbeddedArrayIterable)
      // comparableMap.get(Constants.RESERVED_FIELD_ACL_WRITE)).asJSONArray());

      if (comparableMap.get(Constants.RESERVED_FIELD_ACL_READ) != null) {
        EmbeddedArrayIterable iterable =
            (EmbeddedArrayIterable) comparableMap.get(Constants.RESERVED_FIELD_ACL_READ);
        JSONArray jsonArray = EntityIterables.toJSONArray(iterable);
        read = ACLHelper.onlyIds(jsonArray);
      }

      if (comparableMap.get(Constants.RESERVED_FIELD_ACL_WRITE) != null) {
        EmbeddedArrayIterable iterable =
            (EmbeddedArrayIterable) comparableMap.get(Constants.RESERVED_FIELD_ACL_WRITE);
        JSONArray jsonArray = EntityIterables.toJSONArray(iterable);
        write = ACLHelper.onlyIds(jsonArray);
      }

      if (read == null) {
        read = new String[] {};
      }
      if (write == null) {
        write = new String[] {};
      }
      if (publicRead == null) {
        publicRead = true;
      }
      if (publicWrite == null) {
        publicWrite = false;
      }

      validateSchema(application.getAppId(), namespace, entityType, comparableMap);

      if (entityType.equalsIgnoreCase(defaultUserStore)) {
        if (beforeSave(application, namespace, comparableMap, application.getAppId(), entityType)) {

          String username = (String) comparableMap.get(Constants.RESERVED_FIELD_USERNAME);
          String password = (String) comparableMap.get(Constants.RESERVED_FIELD_PASSWORD);

          if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException();
          }

          JSONArray roleJSONArray =
              comparableMap.get("roles") != null ? (JSONArray) comparableMap.get("roles") : null;
          List<String> roleList = new LinkedList<>();
          try {
            for (int i = 0; i < roleJSONArray.length(); i++) {
              JSONObject jsonValue = roleJSONArray.getJSONObject(i);
              if (jsonValue != null) {
                String roleId = jsonValue.getString(Constants.ROLE_ID);
                roleList.add(roleId);
              } else {
                String roleId = roleJSONArray.getString(i);
                roleList.add(roleId);
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          String entityId =
              userRepository.createUser(
                  application.getAppId(),
                  namespace,
                  entityType,
                  username,
                  password,
                  comparableMap,
                  read,
                  write,
                  publicRead,
                  publicWrite,
                  Iterables.toArray(roleList, String.class),
                  actions,
                  null,
                  null,
                  null);
          JSONObject entityObject = new JSONObject();
          entityObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
          result.put("entity", entityObject);
          comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
          pubSubService.created(application.getAppId(), namespace, entityType, entityId);
          afterSave(application, namespace, comparableMap, application.getAppId(), entityType);
        }
        afterSave(application, namespace, comparableMap, application.getAppId(), entityType);
      } else if (entityType.equalsIgnoreCase(defaultRoleStore)) {
        if (beforeSave(application, namespace, comparableMap, application.getAppId(), entityType)) {
          String roleName = (String) comparableMap.get(Constants.ROLE_NAME);
          roleRepository.createRole(
              application.getAppId(),
              namespace,
              entityType,
              roleName,
              read,
              write,
              publicRead,
              publicWrite,
              actions);
          afterSave(application, namespace, comparableMap, application.getAppId(), entityType);
        }
      } else if (entityType.equalsIgnoreCase(defaultFunctionStore)) {
        if (beforeSave(application, namespace, comparableMap, application.getAppId(), entityType)) {
          String entityId =
              entityRepository.createEntity(
                  application.getAppId(),
                  namespace,
                  entityType,
                  new EntityClassBuilder()
                      .comparableMap(comparableMap)
                      .read(read)
                      .write(write)
                      .publicRead(publicRead)
                      .publicWrite(publicWrite)
                      // .build(), actions, entityActions, Arrays.asList(new
                      // String[]{Constants.RESERVED_FIELD_FUNCTION_NAME}));
                      .build(),
                  actions,
                  entityActions,
                      new CreateOptionBuilder()
                              .createOption(null)
                              .referencePropertyName(null)
                              .build(),
                  new EntityMetadataBuilder()
                      .uniqueProperties(
                          Arrays.asList(new String[] {Constants.RESERVED_FIELD_FUNCTION_NAME}))
                      .build());
          JSONObject entityObject = new JSONObject();
          entityObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
          result.put("entity", entityObject);
          comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
          pubSubService.created(application.getAppId(), namespace, entityType, entityId);
          afterSave(application, namespace, comparableMap, application.getAppId(), entityType);
          return result;
        } else {
          throw new IllegalArgumentException();
        }
      } else {
        if (beforeSave(application, namespace, comparableMap, application.getAppId(), entityType)) {
          String entityId =
              entityRepository.createEntity(
                  application.getAppId(),
                  namespace,
                  entityType,
                  new EntityClassBuilder()
                      .comparableMap(comparableMap)
                      .read(read)
                      .write(write)
                      .publicRead(publicRead)
                      .publicWrite(publicWrite)
                      .blobName(blobName)
                      .blob(blobStream)
                      .build(),
                  actions,
                  entityActions,
                  createOption,
                  metadata);
          JSONObject entityObject = new JSONObject();
          entityObject.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
          result.put("entity", entityObject);
          comparableMap.put(Constants.RESERVED_FIELD_ENTITY_ID, entityId);
          afterSave(application, namespace, comparableMap, application.getAppId(), entityType);
          try {
            pubSubService.created(application.getAppId(), namespace, entityType, entityId);
          } catch (Exception e) {
            e.printStackTrace();
          }
          return result;
        } else {
          throw new IllegalArgumentException();
        }
      }
    } else {
      throw new IllegalArgumentException();
    }
    return null;
  }

  @Override
  public boolean beforeSave(
      Application application,
      String namespace,
      Map<String, Comparable> entity,
      String appId,
      String entityType) {
    String cloudCode = application.getCloudCode();
    LOG.info("Cloud Code : " + cloudCode);
    List<JsFunction> jsFunctions = new LinkedList<>();
    if (cloudCode != null && !cloudCode.isEmpty()) {
      jsFunctions = parseJS(cloudCode);
    }
    final String[] beforeSaveExpr = {null};
    jsFunctions.forEach(
        jsFunction -> {
          if (jsFunction.getFunctionName().equals("beforeSave")) {
            beforeSaveExpr[0] = jsFunction.getExpression();
          }
        });
    TriggerResponse response = new TriggerResponse();
    if (beforeSaveExpr[0] != null && !beforeSaveExpr[0].isEmpty()) {
      AppEntityRepository repository =
          new AppEntityRepository(entityRepository, appId, namespace, entityType);
      AppEmailService emailService = null;
      if (application != null && application.getEmailConfig() != null) {
        Email emailConfig = application.getEmailConfig();
        emailService = new AppEmailService(emailConfig);
      }
      TriggerRequest request = new TriggerRequest(entity, entityType, repository, emailService);
      Object evaluated = eval(request, response, beforeSaveExpr[0]);
      response = (TriggerResponse) evaluated;
      LOG.info("Evaluated: " + String.valueOf(((TriggerResponse) evaluated).isSuccess()));
      LOG.info("Response Body: " + String.valueOf(((TriggerResponse) evaluated).getBody()));
      return response.isSuccess();
    } else {
      return true;
    }
  }

  @Override
  public boolean afterSave(
      Application application,
      String namespace,
      Map<String, Comparable> entity,
      String appId,
      String entityType) {
    String cloudCode = application.getCloudCode();
    final String[] afterSaveExpr = {null};
    LOG.info("Cloud Code : " + cloudCode);
    List<JsFunction> jsFunctions = new LinkedList<>();
    if (cloudCode != null && !cloudCode.isEmpty()) {
      jsFunctions = parseJS(cloudCode);
    }
    final String[] beforeSaveExpr = {null};
    jsFunctions.forEach(
        jsFunction -> {
          if (jsFunction.getFunctionName().equals("afterSave")) {
            afterSaveExpr[0] = jsFunction.getExpression();
          }
        });
    TriggerResponse response = new TriggerResponse();

    if (afterSaveExpr[0] != null && !afterSaveExpr[0].isEmpty()) {
      AppEntityRepository repository =
          new AppEntityRepository(entityRepository, appId, namespace, entityType);
      AppEmailService emailService = null;
      if (application != null && application.getEmailConfig() != null) {
        Email emailConfig = application.getEmailConfig();
        emailService = new AppEmailService(emailConfig);
      }
      TriggerRequest request = new TriggerRequest(entity, entityType, repository, emailService);
      Object evaluated = eval(request, response, afterSaveExpr[0]);
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
      cx.evaluateString(
          scope,
          String.format("function onRequest() { %s return response; } ", expression),
          "<func>",
          1,
          null);

      // call method run()
      Object fObj = scope.get("onRequest", scope);
      Function f = (Function) fObj;
      Object result = f.call(cx, scope, (Scriptable) jsReqObj, null);
      if (result instanceof Wrapper) return ((Wrapper) result).unwrap();
      return result;

    } finally {
      Context.exit();
    }
  }

  protected List<JsFunction> parseJS(String jsCode) {
    List<JsFunction> jsFunctions = new LinkedList<>();
    AstRoot astRoot = new Parser().parse(jsCode, null, 1);
    List<AstNode> statList = astRoot.getStatements();

    Map<String, String> functionBodyMap = new HashMap<>();

    for (Iterator<AstNode> iter = statList.iterator(); iter.hasNext(); ) {
      AstNode astNode = iter.next();
      if (astNode.getType() == Token.FUNCTION) {
        FunctionNode fNode = (FunctionNode) astNode;
        System.out.println(
            "*** function Name : "
                + fNode.getName()
                + ", paramCount : "
                + fNode.getParams()
                + ", depth : "
                + fNode.depth());
        AstNode bNode = fNode.getBody();
        Block block = (Block) bNode;
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

  @Override
  public void validateSchema(
      String appId, String namespace, String entityType, Map<String, Comparable> comparableMap)
      throws IllegalArgumentException {
    List<EntityType> entityTypes = schemaService.listSchemas(appId, namespace);
    entityTypes.forEach(
        type -> {
          if (type.getEntityType().equalsIgnoreCase("users")) {
            List<EntityPropertyType> propertyTypes = type.getPropertyTypes();
            propertyTypes.forEach(
                propertyType -> {
                  propertyType.getPropertyName();
                });

          } else if (type.getEntityType().equalsIgnoreCase("roles")) {
            List<EntityPropertyType> propertyTypes = type.getPropertyTypes();

          } else if (type.getEntityType().equalsIgnoreCase(entityType)) {
            List<EntityPropertyType> propertyTypes = type.getPropertyTypes();
          }
        });
    comparableMap.forEach(
        (key, value) -> {
          List<EntityPropertyType> types =
              schemaService.listPropertyTypes(appId, namespace, entityType);
          types.forEach(
              type -> {
                if (type != null
                        && type.getPropertyType() != null
                        && type.getPropertyType().toString().equals(key)) {
                  EntityPropertyType.TYPE expectedPropertyType = type.getPropertyType();
                  if (value instanceof EmbeddedEntityIterable) {
                    if (!expectedPropertyType.equals(EntityPropertyType.TYPE.OBJECT)) {
                      throw new IllegalArgumentException(
                          "Property " + key + " should be a " + type.toString());
                    }
                  } else if (value instanceof EmbeddedArrayIterable) {
                    if (!expectedPropertyType.equals(EntityPropertyType.TYPE.ARRAY)) {
                      throw new IllegalArgumentException(
                          "Property " + key + " should be a " + type.toString());
                    }
                  } else if (value instanceof Boolean) {
                    if (!expectedPropertyType.equals(EntityPropertyType.TYPE.BOOLEAN)) {
                      throw new IllegalArgumentException(
                          "Property " + key + " should be a " + type.toString());
                    }
                  } else if (value instanceof String) {
                    if (!expectedPropertyType.equals(EntityPropertyType.TYPE.STRING)) {
                      throw new IllegalArgumentException(
                          "Property " + key + " should be a " + type.toString());
                    }
                  } else if (value instanceof Number) {
                    if (!expectedPropertyType.equals(EntityPropertyType.TYPE.NUMBER)) {
                      throw new IllegalArgumentException(
                          "Property " + key + " should be a " + type.toString());
                    }
                  }
                }
              });
        });
  }
}
