/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.resource.jee;

import com.alibaba.fastjson.JSONObject;
import com.divroll.backend.Constants;
import com.divroll.backend.job.ActivationTransactionalEmailJob;
import com.divroll.backend.job.RetryJobWrapper;
import com.divroll.backend.job.TransactionalEmailJob;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.repository.SuperuserRepository;
import com.divroll.backend.resource.SuperuserResource;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.mindrot.jbcrypt.BCrypt;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.util.Date;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class JeeSuperuserServerResource extends BaseServerResource
    implements SuperuserResource {

    private static final Logger LOG = LoggerFactory.getLogger(JeeSuperuserServerResource.class);

    @Inject
    SuperuserRepository superuserRepository;

    @Inject
    WebTokenService webTokenService;

    @Inject
    @Named("masterSecret")
    String masterSecret;

    @Inject
    @Named("masterToken")
    String theMasterToken;

    @Inject
    @Named("postmark.serverToken")
    private String postmarkServerToken;

    @Inject
    @Named("postmark.senderSignature")
    private String postmarkSenderSignature;

    @Inject
    @Named("postmark.resetPasswordTemplateId")
    private String postmarkActivationTemplateId;

    @Inject
    @Named("defaultActivationBase")
    private String defaultActivationBase;

    @Override
    public Representation getUser() {
        try {
            String username = getQueryValue(Constants.QUERY_USERNAME);
            String password = getQueryValue(Constants.QUERY_PASSWORD);
            String auth = getQueryValue(Constants.QUERY_AUTH_TOKEN);

            if((username == null || password == null) && auth == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }

            boolean isMasterToken = false;
            if (theMasterToken != null
                    && masterToken != null
                    && BCrypt.checkpw(masterToken, theMasterToken)) {
                isMasterToken = true;
            }

            Superuser superuser = superuserRepository.getUserByAuthToken(auth);
            if(username != null) {
                superuser = superuserRepository.getUserByUsername(username);
            }

            if(superuser != null) {
                String superuserId = superuser.getEntityId();
                String email = superuser.getUsername();
                String existingPassword = superuser.getPassword();
                if(auth != null) {
                    String authToken = webTokenService.createToken(masterSecret, superuserId);
                    superuser.setAuthToken(authToken);
                    superuser.setPassword(null);
                    superuser.setEntityId(superuserId);
                    superuser.setDateUpdated(superuser.getDateUpdated());
                    superuser.setDateCreated(superuser.getDateCreated());
                    setStatus(Status.SUCCESS_OK);
                    if(superuser.getActive() == null || superuser.getActive() == false) {
                        if(isMasterToken) {
                            return new JsonRepresentation(asJSONObject(superuser));
                        } else {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Superuser " + username + " is not activated");
                            sendActivationCode(email);
                        }
                    } else if(superuser.getActive() == true) {
                        return new JsonRepresentation(asJSONObject(superuser));
                    }
                } else if (BCrypt.checkpw(password, existingPassword)) {
                    String authToken = webTokenService.createToken(masterSecret, superuserId);
                    superuser.setAuthToken(authToken);
                    superuser.setPassword(null);
                    superuser.setEntityId(superuserId);
                    superuser.setDateUpdated(superuser.getDateUpdated());
                    superuser.setDateCreated(superuser.getDateCreated());
                    setStatus(Status.SUCCESS_OK);
                    if(superuser.getActive() == null || superuser.getActive() == false) {
                        if(isMasterToken) {
                            return new JsonRepresentation(asJSONObject(superuser));
                        } else {
                            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Superuser " + username + " is not activated");
                            sendActivationCode(email);
                        }
                    } else if(superuser.getActive() == true) {
                        return new JsonRepresentation(asJSONObject(superuser));
                    }
                } else {
                    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                    return null;
                }
            } else {
                notFound(Constants.ERROR_USER_NOT_EXISTS);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public Representation updateUser(Superuser entity) {
        return null;
    }

    @Override
    public void deleteUser(Superuser entity) {

    }

    private void sendActivationCode(String email) throws Exception {
        // Send activation code
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + ONE_DAY);
        String activationToken
                = webTokenService.createEmailToken(masterSecret, email, String.valueOf(expiration.getTime()));
        LOG.info("ACTIVATION TOKEN: " + activationToken);

        JobDetail job =
                newJob(RetryJobWrapper.class)
                        .storeDurably()
                        .requestRecovery(true)
                        .withIdentity(UUID.randomUUID().toString(), "transactionalEmailJobs")
                        .withDescription(
                                "An important job that fails with an exception and is retried.")
                        .usingJobData(RetryJobWrapper.WRAPPED_JOB_KEY, ActivationTransactionalEmailJob.class.getName())
                        .usingJobData(RetryJobWrapper.MAX_RETRIES_KEY, "5")
                        .usingJobData(RetryJobWrapper.RETRY_DELAY_KEY, "5")
                        .usingJobData("serverToken", postmarkServerToken)
                        .usingJobData("senderSignature", postmarkSenderSignature)
                        .usingJobData("recipient", email)
                        .usingJobData("templateId", Integer.valueOf(postmarkActivationTemplateId))
                        .usingJobData("activationToken", activationToken)
                        .usingJobData("activationBaseUrl", defaultActivationBase)
                        .usingJobData("subject", "Account Activation")
                        .build();

        Trigger trigger =
                newTrigger()
                        .withIdentity(UUID.randomUUID().toString(),  "transactionalEmailJobs")
                        .startNow()
                        .withSchedule(simpleSchedule())
                        .build();

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.scheduleJob(job, trigger);
    }

}


