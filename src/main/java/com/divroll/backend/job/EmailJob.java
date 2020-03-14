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
package com.divroll.backend.job;

import com.divroll.backend.email.EmailConfig;
import com.divroll.backend.service.EmailService;
import com.divroll.backend.service.jee.JeeEmailService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EmailJob implements StatefulJob {

  private static final Logger LOG = LoggerFactory.getLogger(EmailJob.class);

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    LOG.info("EmailJob - execute " + jobExecutionContext);
    try {
      JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

      String smtpHost = (String) dataMap.get("smtpHost");
      String tlsPort = (String) dataMap.get("tlsPort");
      String fromEmail = (String) dataMap.get("fromEmail");
      String password = (String) dataMap.get("password");
      String toEmail = (String) dataMap.get("toEmail");
      String subject = (String) dataMap.get("subject");
      String htmlBody = (String) dataMap.get("htmlBody");

      EmailService emailService = new JeeEmailService();
      EmailConfig emailConfig = new EmailConfig();
      emailConfig.setSmtpHost(smtpHost);
      emailConfig.setTlsPort(tlsPort);

      emailService.sendEmail(emailConfig, fromEmail, password, toEmail, subject, htmlBody);

    } catch (Exception e) {
      e.printStackTrace();
      throw new JobExecutionException("error occurred in running the job", true);
    }
  }
}
