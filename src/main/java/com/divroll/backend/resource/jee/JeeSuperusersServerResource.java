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

import com.divroll.backend.guice.SelfInjectingServerResource;
import com.divroll.backend.job.ActivationTransactionalEmailJob;
import com.divroll.backend.job.RetryJobWrapper;
import com.divroll.backend.job.TransactionalEmailJob;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.model.exception.DuplicateSuperuserException;
import com.divroll.backend.repository.SuperuserRepository;
import com.divroll.backend.resource.SuperusersResource;
import com.divroll.backend.service.WebTokenService;
import com.divroll.backend.util.RegexHelper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.Date;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class JeeSuperusersServerResource extends BaseServerResource
    implements SuperusersResource {

    private static final Logger LOG = LoggerFactory.getLogger(JeeSuperusersServerResource.class);

    private static Long ONE_DAY = 1000L * 60L * 60L * 24L;

    @Inject
    @Named("defaultSuperuserStore")
    String defaultSuperuserStore; //masterSecret

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
    @Named("postmark.postmarkActivationTemplateId")
    private String postmarkActivationTemplateId;

    @Inject
    SuperuserRepository superuserRepository;

    @Inject
    WebTokenService webTokenService;

    @Override
    public Superuser createUser(Superuser entity) {

        if(entity == null)  {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }
        try {
            if(entity.getUsername() != null
                    && entity.getPassword() != null) {

                // TODO: Username should be email only

                entity.setActive(false);
                String userId = superuserRepository.createUser(defaultSuperuserStore, entity.getUsername(), entity.getPassword());
                entity.setEntityId(userId);
                entity.setPassword(null);

                // Send activation code
                Date expiration = new Date();
                expiration.setTime(expiration.getTime() + ONE_DAY);
                String activationToken
                        = webTokenService.createEmailToken(masterSecret, entity.getUsername(), String.valueOf(expiration.getTime()));
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
                                .usingJobData("templateId", Integer.valueOf(postmarkActivationTemplateId))
                                .usingJobData("recipient", entity.getUsername())
                                .usingJobData("activationToken", activationToken)
                                .usingJobData("activationBaseUrl", defaultActivationBase)
                                .build();

                Trigger trigger =
                        newTrigger()
                                .withIdentity(UUID.randomUUID().toString(),  "emffailJobs")
                                .startNow()
                                .withSchedule(simpleSchedule())
                                .build();

                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.scheduleJob(job, trigger);
                return entity;
            }
        } catch (DuplicateSuperuserException e) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            setStatus(Status.SERVER_ERROR_INTERNAL);
            LOG.error(e, e.getMessage());
        }
        return null;
    }

    @Override
    public Representation listUsers() {
        return null;
    }
//    @Override
//    public Representation getUser() {
//        return null;
//    }
//
//    @Override
//    public Representation updateUser(User entity) {
//        return null;
//    }
//
//    @Override
//    public void deleteUser(User entity) {
//
//    }
}
