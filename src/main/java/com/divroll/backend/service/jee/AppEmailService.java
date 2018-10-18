package com.divroll.backend.service.jee;

import com.divroll.backend.job.EmailJob;
import com.divroll.backend.job.RetryJobWrapper;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.Email;
import com.divroll.backend.model.User;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AppEmailService {

    private Email emailConfig;

    private AppEmailService() {}

    public AppEmailService(Email emailConfig) {
        setEmailConfig(emailConfig);
    }

    public void send(String subject, String to, String htmlBody) {
        if(emailConfig != null) {
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
                    .usingJobData("toEmail", to)
                    .usingJobData("subject", subject)
                    .usingJobData("htmlBody", htmlBody)
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(UUID.randomUUID().toString(), "emailJobs")
                    .startNow()
                    .withSchedule(simpleSchedule())
                    .build();
            try {
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
