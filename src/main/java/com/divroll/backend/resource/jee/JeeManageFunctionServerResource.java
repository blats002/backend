package com.divroll.backend.resource.jee;

import com.divroll.backend.model.Application;
import com.divroll.backend.model.Function;
import com.divroll.backend.repository.FunctionRepository;
import com.divroll.backend.resource.ManageFunctionResource;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.restlet.data.Status;

import javax.inject.Inject;

public class JeeManageFunctionServerResource extends BaseServerResource
    implements ManageFunctionResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeManageFunctionServerResource.class);

    @Inject
    FunctionRepository functionRepository;

    String functionName;

    @Override
    protected void doInit() {
        super.doInit();
        functionName = getAttribute("functionName");
    }

    @Override
    public Function createFunction(Function entity) {
        try {
            if (!isAuthorized()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if(!isMaster()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if(entity == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }
            functionRepository.createFunction(appId, functionName, entity.getJar());
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public void removeFunction(Function entity) {
        try {
            if (!isAuthorized()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            if(!isMaster()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            if(functionName == null || functionName.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            functionRepository.deleteFunction(appId, functionName);
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }
}
