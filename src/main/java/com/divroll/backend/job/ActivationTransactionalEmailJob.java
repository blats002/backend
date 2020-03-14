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
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * GPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.job;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.json.JSONObject;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class ActivationTransactionalEmailJob implements StatefulJob {

    private static final Logger LOG = LoggerFactory.getLogger(ActivationTransactionalEmailJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("TransactionalEmailJob - execute " + jobExecutionContext);
        try {
            JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            String serverToken = (String) dataMap.get("serverToken");
            String senderSignature = (String) dataMap.get("senderSignature");
            String recipient = (String) dataMap.get("recipient");
            Integer templateId = (Integer) dataMap.get("templateId");
            String activationToken = (String) dataMap.get("activationToken");
            String activationBaseUrl = (String) dataMap.get("activationBaseUrl");

            JSONObject templateModel = new JSONObject();
            templateModel.put("action_url", activationBaseUrl + "activationToken=" + activationToken);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("From", senderSignature);
            jsonObject.put("To", recipient);
            jsonObject.put("TemplateId", templateId);
            jsonObject.put("TemplateModel", templateModel);

            LOG.info(jsonObject.toString());

            HttpRequestWithBody postmarkRequest = Unirest.post("https://api.postmarkapp.com/email/withTemplate");
            postmarkRequest.header("Accept", " application/json");
            postmarkRequest.header("X-Postmark-Server-Token", serverToken);
            HttpResponse<JsonNode> response = postmarkRequest.body(jsonObject).asJson();

            if(response.getStatus() == 200) {

            } else {
                LOG.error(response.getStatusText());
                throw new JobExecutionException("error occurred in running the job", true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new JobExecutionException("error occurred in running the job", true);
        }
    }
}
