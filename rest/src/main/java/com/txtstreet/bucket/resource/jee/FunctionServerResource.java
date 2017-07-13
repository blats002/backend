package com..bucket.resource.jee;

import com.apiblast.customcode.example.methods.CreateResetPasswordLinkMethod;
import com.apiblast.customcode.example.methods.ResetPasswordMethod;
import com.apiblast.sdkapi.MethodVerb;
import com.apiblast.sdkapi.customcode.CustomCodeMethod;
import com.apiblast.sdkapi.rest.CustomCodeRequest;
import com.apiblast.sdkapi.rest.CustomCodeResponse;
import com..bucket.resource.FunctionResource;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
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
        ResetPasswordMethod resetPasswordMethod = new ResetPasswordMethod();
        CreateResetPasswordLinkMethod createResetPasswordLinkMethod = new CreateResetPasswordLinkMethod();
        methods.put(resetPasswordMethod.getMethodName(), resetPasswordMethod);
        methods.put(createResetPasswordLinkMethod.getMethodName(), createResetPasswordLinkMethod);
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
                return success(status);
            }
            return success();
        } catch (Exception e) {
            e.printStackTrace();
            return internalError();
        }
    }

    @Override
    public Representation get(Representation entity) {
        return null;
    }

    @Override
    public Representation put(Representation entity) {
        return null;
    }

    @Override
    public Representation delete(Representation entity) {
        return null;
    }

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
