package com.divroll.bucket.resource.jee;

import com.apiblast.customcode.example.methods.*;
import com.apiblast.sdkapi.MethodVerb;
import com.apiblast.sdkapi.customcode.CustomCodeMethod;
import com.apiblast.sdkapi.rest.CustomCodeRequest;
import com.apiblast.sdkapi.rest.CustomCodeResponse;
import com.divroll.bucket.resource.FunctionResource;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class FunctionServerResource extends BaseServerResource
        implements FunctionResource {

    private Map<String,CustomCodeMethod> methods = new LinkedHashMap<String,CustomCodeMethod>();
    private Map<String, String> params = new LinkedHashMap<String, String>();

    @Override
    protected void doInit() throws ResourceException {
        //SignupMethod signupMethod = new SignupMethod();
        //LoginMethod loginMethod = new LoginMethod();
        //ApplicationsMethod applicationMethod = new ApplicationsMethod();
        ResetPasswordMethod resetPasswordMethod = new ResetPasswordMethod();
        CreateResetPasswordLinkMethod createResetPasswordLinkMethod = new CreateResetPasswordLinkMethod();
        CreateVerifyEmailLinkMethod createVerifyEmailLinkMethod = new CreateVerifyEmailLinkMethod();
        VerifyEmailMethod verifyEmailMethod = new VerifyEmailMethod();
        methods.put(resetPasswordMethod.getMethodName(), resetPasswordMethod);
        methods.put(createResetPasswordLinkMethod.getMethodName(), createResetPasswordLinkMethod);
        methods.put(createVerifyEmailLinkMethod.getMethodName(), createVerifyEmailLinkMethod);
        methods.put(verifyEmailMethod.getMethodName(), verifyEmailMethod);
        //methods.put(signupMethod.getMethodName(), signupMethod);
        //methods.put(loginMethod.getMethodName(), loginMethod);
        //methods.put(applicationMethod.getMethodName(), applicationMethod);
        Form parameters = getQuery();
        Iterator<Parameter> it = parameters.iterator();
        while(it.hasNext()){
            Parameter parameter = it.next();
            String name = parameter.getName();
            String value = parameter.getValue();
            params.put(name, value);
        }
        super.doInit();
    }

    @Override
    public Representation post(Representation entity) {
        try {
            CustomCodeMethod customCodeMethod = methods.get(functioName);
            if(customCodeMethod != null) {
                CustomCodeResponse response = customCodeMethod.execute(processedRequest());
                int status = response.getResponseStatus();
                Map<String,?> map = response.getResponseMap();
                if(status == 200) {
                    if(map.get("reasonPhrase") != null) {
                        String reasonPhrase = (String) map.get("reasonPhrase");
                        return success(status, reasonPhrase);
                    }
                    return success();
                } else if(status == 400) {
                    if(map.get("reasonPhrase") != null) {
                        String reasonPhrase = (String) map.get("reasonPhrase");
                        return badRequest(reasonPhrase);
                    }
                    return badRequest();
                } else if(status == 500) {
                    return internalError();
                } else {
                    return internalError();
                }
            } else {
                return notFound();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalError();
        }
    }

    @Override
    public Representation get(Representation entity) {
        try {
            CustomCodeMethod customCodeMethod = methods.get(functioName);
            if(customCodeMethod != null) {
                CustomCodeResponse response = customCodeMethod.execute(processedRequest());
                int status = response.getResponseStatus();
                Map<String,?> map = response.getResponseMap();
                if(status == 200) {
                    if(map.get("reasonPhrase") != null) {
                        String reasonPhrase = (String) map.get("reasonPhrase");
                        return success(status, reasonPhrase);
                    }
                    return success();
                } else if(status == 400) {
                    if(map.get("reasonPhrase") != null) {
                        String reasonPhrase = (String) map.get("reasonPhrase");
                        return badRequest(reasonPhrase);
                    }
                    return badRequest();
                } else if(status == 500) {
                    return internalError();
                } else {
                    return internalError();
                }
            } else {
                return notFound();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalError();
        }    }

    @Override
    public Representation put(Representation entity) {
        try {
            CustomCodeMethod customCodeMethod = methods.get(functioName);
            if(customCodeMethod != null) {
                CustomCodeResponse response = customCodeMethod.execute(processedRequest());
                int status = response.getResponseStatus();
                Map<String,?> map = response.getResponseMap();
                if(status == 200) {
                    if(map.get("reasonPhrase") != null) {
                        String reasonPhrase = (String) map.get("reasonPhrase");
                        return success(status, reasonPhrase);
                    }
                    return success();
                } else if(status == 400) {
                    if(map.get("reasonPhrase") != null) {
                        String reasonPhrase = (String) map.get("reasonPhrase");
                        return badRequest(reasonPhrase);
                    }
                    return badRequest();
                } else if(status == 500) {
                    return internalError();
                } else {
                    return internalError();
                }
            } else {
                return notFound();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalError();
        }    }

    @Override
    public Representation delete(Representation entity) {
        try {
            CustomCodeMethod customCodeMethod = methods.get(functioName);
            if(customCodeMethod != null) {
                CustomCodeResponse response = customCodeMethod.execute(processedRequest());
                int status = response.getResponseStatus();
                Map<String,?> map = response.getResponseMap();
                if(status == 200) {
                    if(map.get("reasonPhrase") != null) {
                        String reasonPhrase = (String) map.get("reasonPhrase");
                        return success(status, reasonPhrase);
                    }
                    return success();
                } else if(status == 400) {
                    if(map.get("reasonPhrase") != null) {
                        String reasonPhrase = (String) map.get("reasonPhrase");
                        return badRequest(reasonPhrase);
                    }
                    return badRequest();
                } else if(status == 500) {
                    return internalError();
                } else {
                    return internalError();
                }
            } else {
                return notFound();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return internalError();
        }    }

    private CustomCodeRequest processedRequest() {
        Representation representation = getRequestEntity();
        String body = null;
        try {
            body = representation.getText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String path = getRequest().getResourceRef().getPath();
        CustomCodeRequest proccessedRequest = new CustomCodeRequest
                (MethodVerb.POST, path, params, body, "", "", functioName, 0L);
        return proccessedRequest;
    }

}
