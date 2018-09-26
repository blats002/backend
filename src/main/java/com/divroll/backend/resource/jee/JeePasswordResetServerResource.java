package com.divroll.backend.resource.jee;

import com.divroll.backend.model.Application;
import com.divroll.backend.model.PasswordResetDTO;
import com.divroll.backend.model.User;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.PasswordResetResource;
import com.divroll.backend.service.ApplicationService;
import com.divroll.backend.service.WebTokenService;
import com.google.common.io.BaseEncoding;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.logging.Logger;

public class JeePasswordResetServerResource extends BaseServerResource
    implements PasswordResetResource {

    private static final Logger LOG
            = Logger.getLogger(JeePasswordResetServerResource.class.getName());


    @Inject
    @Named("defaultUserStore")
    String storeName;

    @Inject
    UserRepository userRepository;

    @Inject
    WebTokenService webTokenService;

    @Inject
    ApplicationService applicationService;

    @Override
    public void validateResetPassword(PasswordResetDTO entity) {
        try {
            String token = getQueryValue("token");
            if(token == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            String decoded = new String(BaseEncoding.base64().decode(token), "UTF-8");
            JSONObject tokenObj = new JSONObject(decoded);

            String appId = tokenObj.getString("appId");
            String usernameWebToken = tokenObj.getString("usernameWebToken");
            String passwordWebToken = tokenObj.getString("passwordWebToken");

            Application application = applicationService.read(appId);
            if(application != null) {
                String username = webTokenService.readUserIdFromToken(application.getMasterKey(), usernameWebToken);
                User userEntity = userRepository.getUserByUsername(appId, storeName, username);
                String newPassword = webTokenService.readUserIdFromToken(userEntity.getPassword(), passwordWebToken);

                //LOG.info("username->" + username);
                //LOG.info("newPassword->" + newPassword);

                if (userEntity == null) {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                } else {
                    if(newPassword == null) {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        return;
                    }
                    String newHashPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                    //LOG.info("newHashPassword->" + newHashPassword);
                    Boolean success = userRepository.updateUserPassword(appId, storeName, userEntity.getEntityId(), newHashPassword);
                    if(success) {
                        setStatus(Status.SUCCESS_OK);
                    } else {
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                }
            } else {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public void resetPassword(PasswordResetDTO entity) {
        try {
            if(entity == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            if(!isAuthorized()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            if (getApp() == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            if (validate(entity.getUsername(), entity.getNewPassword())) {
                if(entity.getPassword() != null && !entity.getPassword().isEmpty()) {
                    User userEntity = userRepository.getUserByUsername(appId, storeName, entity.getUsername());
                    if (userEntity == null) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    } else {
                        String currentPassword = userEntity.getPassword();
                        String newHashPassword = BCrypt.hashpw(entity.getPassword(), BCrypt.gensalt());
                        if(BCrypt.checkpw(entity.getPassword(), currentPassword)) {
                            Boolean success = userRepository.updateUserPassword(appId, storeName, userEntity.getEntityId(), newHashPassword);
                            if(success) {
                                setStatus(Status.SUCCESS_OK);
                            } else {
                                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                            }
                            return;
                        } else {
                            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                            return;
                        }
                    }
                } else {
                    User userEntity = userRepository.getUserByUsername(appId, storeName, entity.getUsername());
                    if (userEntity == null) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    } else {
                        String username = entity.getUsername();
                        String newPassword = entity.getNewPassword(); // plain password
                        String currentPassword = userEntity.getPassword(); // encrypted password
                        String usernameWebToken = webTokenService.createToken(getApp().getMasterKey(), username);
                        String passwordWebToken = webTokenService.createToken(currentPassword, newPassword);

                        JSONObject tokenObj = new JSONObject();
                        tokenObj.put("appId", getApp().getAppId());
                        tokenObj.put("usernameWebToken", usernameWebToken);
                        tokenObj.put("passwordWebToken", passwordWebToken);

                        String encoded = BaseEncoding.base64().encode(tokenObj.toString().getBytes("UTF-8"));

                        LOG.info("Generated Password Reset Token - " + encoded); // TODO: Do not log in production

                        // TODO: Send token to user associated email address
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    private boolean validate(String username, String password) {
        return username != null && password != null && !username.isEmpty() && !password.isEmpty();
    }

}
