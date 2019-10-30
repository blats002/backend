package com.divroll.backend.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class TransactionalEmailJob implements StatefulJob {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // TODO
    }
}
