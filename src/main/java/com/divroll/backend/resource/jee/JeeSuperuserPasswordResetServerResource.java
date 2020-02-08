/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
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

import com.divroll.backend.Constants;
import com.divroll.backend.guice.SelfInjectingServerResource;
import com.divroll.backend.job.PasswordResetTransactionalEmailJob;
import com.divroll.backend.job.RetryJobWrapper;
import com.divroll.backend.job.TransactionalEmailJob;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.repository.SuperuserRepository;
import com.divroll.backend.resource.SuperuserPasswordResetResource;
import com.divroll.backend.service.WebTokenService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class JeeSuperuserPasswordResetServerResource extends BaseServerResource
    implements SuperuserPasswordResetResource {

    private static final Logger LOG = LoggerFactory.getLogger(JeeSuperuserPasswordResetServerResource.class);

    @Inject
    @Named("masterSecret")
    String masterSecret;

    @Inject
    @Named("postmark.serverToken")
    private String postmarkServerToken;

    @Inject
    @Named("postmark.senderSignature")
    private String postmarkSenderSignature;

    @Inject
    @Named("postmark.resetPasswordTemplateId")
    private String postmarkResetPasswordTemplateId;

    @Inject
    SuperuserRepository superuserRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Representation resetPassword() {
        try {
            String newPassword = getQueryValue("newPassword");
            String email = getQueryValue("username");
            String resetToken = getQueryValue("resetToken");
            if(email != null && !email.isEmpty() && newPassword != null && !newPassword.isEmpty()) {
                Date expiration = new Date();
                expiration.setTime(expiration.getTime() + ONE_DAY);
                String newResetToken = webTokenService.createPasswordResetToken(masterSecret, email, newPassword,
                        String.valueOf(expiration.getTime()));
                sendPasswordResetToken(email, newResetToken);
                setStatus(Status.SUCCESS_ACCEPTED);
            } else if(resetToken != null) {
                Map<String,Object> parsed = webTokenService.readToken(masterSecret, resetToken);
                String parsedEmail = (String) parsed.get(Constants.JWT_ID_EMAIL);
                String parsedNewPassword = (String) parsed.get(Constants.JWT_ID_PASSWORD);
                String parsedExpiration = (String) parsed.get(Constants.JWT_ID_EXPIRATION);
                // TODO: Check if token is expired
                Superuser superuser = superuserRepository.getUserByEmail(parsedEmail);
                if(superuser != null) {
                    String userId = superuser.getEntityId();
                    if(superuserRepository.updateUserPassword(userId, parsedNewPassword)) {
                        setStatus(Status.SUCCESS_ACCEPTED);
                        String authToken = webTokenService.createToken(masterSecret, superuser.getEntityId());
                        superuser.setAuthToken(authToken);
                        return new JsonRepresentation(asJSONObject(superuser));
                    }
                } else {
                    setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                }
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);

            }

        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    private void sendPasswordResetToken(String email, String resetToken) throws Exception {
        // Send activation code
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + ONE_DAY);
        LOG.info("PASSWORD RESET TOKEN: " + resetToken);
        JobDetail job =
                newJob(RetryJobWrapper.class)
                        .storeDurably()
                        .requestRecovery(true)
                        .withIdentity(UUID.randomUUID().toString(), "transactionalEmailJobs")
                        .withDescription(
                                "An important job that fails with an exception and is retried.")
                        .usingJobData(RetryJobWrapper.WRAPPED_JOB_KEY, PasswordResetTransactionalEmailJob.class.getName())
                        .usingJobData(RetryJobWrapper.MAX_RETRIES_KEY, "5")
                        .usingJobData(RetryJobWrapper.RETRY_DELAY_KEY, "5")
                        .usingJobData("serverToken", postmarkServerToken)
                        .usingJobData("senderSignature", postmarkSenderSignature)
                        .usingJobData("recipient", email)
                        .usingJobData("templateId", Integer.valueOf(postmarkResetPasswordTemplateId))
                        .usingJobData("resetToken", resetToken)
                        .usingJobData("passwordResetBaseUrl", defaultPasswordResetBase)
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
