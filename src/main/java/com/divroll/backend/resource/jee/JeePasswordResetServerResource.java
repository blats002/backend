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

import com.divroll.backend.helper.ComparableMapBuilder;
import com.divroll.backend.job.EmailJob;
import com.divroll.backend.job.RetryJobWrapper;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.Email;
import com.divroll.backend.model.PasswordResetDTO;
import com.divroll.backend.model.User;
import com.divroll.backend.repository.UserRepository;
import com.divroll.backend.resource.PasswordResetResource;
import com.divroll.backend.service.ApplicationService;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.io.BaseEncoding;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.restlet.data.Status;

import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeePasswordResetServerResource extends BaseServerResource
    implements PasswordResetResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeePasswordResetServerResource.class);


    @Inject
    @Named("defaultUserStore")
    String defaultUserStore;

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
                User userEntity = userRepository.getUserByUsername(appId, namespace, defaultUserStore, username);
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
                    if(beforeSave(ComparableMapBuilder.newBuilder().put("entityId", entityId).put("username", username).build(), appId, entityType)) {
                        Boolean success = userRepository.updateUserPassword(appId, namespace, defaultUserStore, userEntity.getEntityId(), newHashPassword);
                        if(success) {
                            afterSave(ComparableMapBuilder.newBuilder().put("entityId", entityId).put("username", username).build(), appId, entityType);
                            setStatus(Status.SUCCESS_OK);
                        } else {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        }
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
                    User userEntity = userRepository.getUserByUsername(appId, namespace, defaultUserStore, entity.getUsername());
                    if (userEntity == null) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    } else {
                        String currentPassword = userEntity.getPassword();
                        String newHashPassword = BCrypt.hashpw(entity.getPassword(), BCrypt.gensalt());
                        if(BCrypt.checkpw(entity.getPassword(), currentPassword)) {
                            Boolean success = false;
                            if(beforeSave(ComparableMapBuilder.newBuilder().put("entityId", entityId).put("username", username).build(), appId, entityType)) {
                                success = userRepository.updateUserPassword(appId, namespace, defaultUserStore, userEntity.getEntityId(), newHashPassword);
                            } else {
                                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                            }
                            if(success) {
                                afterSave(ComparableMapBuilder.newBuilder().put("entityId", entityId).put("username", username).build(), appId, entityType);
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
                    User userEntity = userRepository.getUserByUsername(appId, namespace, defaultUserStore, entity.getUsername());
                    if (userEntity == null) {
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    } else {

                        if(userEntity.getEmail() == null || userEntity.getEmail().isEmpty()) {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "User does not have associated email");
                            return;
                        }

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

                        String htmlBody = "<p>Reset password link: http://localhost:8080/divroll/entities/users/resetPassword?" + encoded + "</p>";

                        Application application = getApp();
                        if(application != null && application.getEmailConfig() != null) {
                            Email emailConfig = application.getEmailConfig();
                            JobDetail job = newJob(RetryJobWrapper.class)
                                    .storeDurably()
                                    .requestRecovery(true)
                                    .withIdentity(UUID.randomUUID().toString(), "emailJobs")
                                    .withDescription("An important job that fails with an exception and is retried.")
                                    .usingJobData(RetryJobWrapper.WRAPPED_JOB_KEY, EmailJob.class.getName())
                                    // Set defaults - can be overridden in trigger definition in schedule file
                                    .usingJobData(RetryJobWrapper.MAX_RETRIES_KEY, "5")
                                    .usingJobData(RetryJobWrapper.RETRY_DELAY_KEY, "5")
                                    .usingJobData("smtpHost", emailConfig.getEmailHost())
                                    .usingJobData("tlsPort", emailConfig.getEmailPort())
                                    .usingJobData("fromEmail", emailConfig.getEmailAddress())
                                    .usingJobData("password", emailConfig.getPassword())
                                    .usingJobData("toEmail", userEntity.getEmail())
                                    .usingJobData("subject", "Password Reset")
                                    .usingJobData("htmlBody", htmlBody)
                                    .build();

                            Trigger trigger = newTrigger()
                                    .withIdentity(UUID.randomUUID().toString(), "emailJobs")
                                    .startNow()
                                    .withSchedule(simpleSchedule())
                                    .build();

                            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                            scheduler.scheduleJob(job, trigger);
                        }


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
