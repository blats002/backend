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
package com.divroll.backend.service.jee;

import com.divroll.backend.job.EmailJob;
import com.divroll.backend.job.RetryJobWrapper;
import com.divroll.backend.model.Email;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class AppEmailService {

  private Email emailConfig;

  private AppEmailService() {}

  public AppEmailService(Email emailConfig) {
    setEmailConfig(emailConfig);
  }

  public void send(String subject, String to, String htmlBody) {
    if (emailConfig != null) {
      try {
        JobDetail job =
            newJob(RetryJobWrapper.class)
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
                .usingJobData("toEmail", to)
                .usingJobData("subject", subject)
                .usingJobData("htmlBody", htmlBody)
                .build();

        Trigger trigger =
            newTrigger()
                .withIdentity(UUID.randomUUID().toString(), "emailJobs")
                .startNow()
                .withSchedule(simpleSchedule())
                .build();

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.scheduleJob(job, trigger);
      } catch (SchedulerException e) {
        e.printStackTrace();
      }
    }
  }

  public void setEmailConfig(Email emailConfig) {
    this.emailConfig = emailConfig;
  }
}
