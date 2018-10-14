package com.divroll.backend.resource.jee;

import com.divroll.backend.model.Application;
import com.divroll.backend.model.Applications;
import com.divroll.backend.model.Email;
import com.divroll.backend.model.UserRootDTO;
import com.divroll.backend.repository.RoleRepository;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.ApplicationsResource;
import com.divroll.backend.service.ApplicationService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import jetbrains.exodus.entitystore.EntityId;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class JeeApplicationsServerResource extends BaseServerResource
        implements ApplicationsResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeApplicationsServerResource.class);

    @Inject
    @Named("xodusRoot")
    String xodusRoot;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    @Named("defaultRoleStore")
    String roleStoreName;

    @Inject
    @Named("defaultUserStore")
    String userStoreName;

    @Inject
    @Named("masterToken")
    String theMasterToken;

    @Inject
    ApplicationService applicationService;

    @Override
    public Applications list() {
        try {
            // TODO: Add auth
            if(theMasterToken != null
                    && masterToken != null
                    && BCrypt.checkpw(masterToken, theMasterToken)) {
                List<Application> results = applicationService.list(filters, skip, limit);
                Applications applications = new Applications();
                applications.setSkip(skip);
                applications.setLimit(limit);
                applications.setResults(results);
                setStatus(Status.SUCCESS_OK);
                return applications;
            } else if(isMaster()) {
                Applications applications = new Applications();
                Application application = applicationService.read(appId);
                applications.getResults().add(application);
                applications.setSkip(skip);
                applications.setLimit(1L);
                setStatus(Status.SUCCESS_OK);
                return applications;
            } else {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public Application createApp(Application application) {

        if(appName == null) {
            appName = getQueryValue("appName");
        }

        if(appName == null) {
            appName = application.getAppName();
            if(appName == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }
        }

        UserRootDTO rootDTO = application.getUser();

        String appId = UUID.randomUUID().toString().replace("-", "");
        String apiKey = UUID.randomUUID().toString().replace("-", "");
        String masterKey = UUID.randomUUID().toString().replace("-", "");

        application.setAppId(appId);
        application.setApiKey(BCrypt.hashpw(apiKey, BCrypt.gensalt()));
        application.setMasterKey(BCrypt.hashpw(masterKey, BCrypt.gensalt()));
        if(appName != null && !appName.isEmpty()) {
            application.setAppName(appName);
        }

        final EntityId id = applicationService.create(application);
        if (id != null) {
            //Application app =  applicationService.read(id.toString());

            if(rootDTO != null) {
                String roleId = roleRepository.createRole(appId, roleStoreName, rootDTO.getRole(), null, null, false, false);
                String userId = userRepository.createUser(appId, userStoreName, rootDTO.getUsername(), rootDTO.getPassword(),
                        null, null, null, false, false,
                        new String[]{roleId});
            }


            if (application != null) {
                application.setAppId(appId);
                application.setApiKey(apiKey);
                application.setMasterKey(masterKey);
                application.setAppName(appName);
                setStatus(Status.SUCCESS_CREATED);
                return application;
            }
        } else {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

}
