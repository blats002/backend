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

public class EmailJob implements StatefulJob {

    private static final Logger LOG
            = LoggerFactory.getLogger(EmailJob.class);

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

            emailService.sendEmail(emailConfig, fromEmail, password,
                    toEmail, subject, htmlBody);

        } catch (Exception e) {
            e.printStackTrace();
            throw new JobExecutionException("error occurred in running the job", true);
        }
    }

}
