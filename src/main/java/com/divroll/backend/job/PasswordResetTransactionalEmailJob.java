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

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.wildbit.java.postmark.Postmark;
import com.wildbit.java.postmark.client.ApiClient;
import com.wildbit.java.postmark.client.data.model.message.Message;
import com.wildbit.java.postmark.client.data.model.message.MessageResponse;
import com.wildbit.java.postmark.client.data.model.templates.TemplatedMessage;
import org.json.JSONObject;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import java.util.HashMap;

public class PasswordResetTransactionalEmailJob implements StatefulJob {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordResetTransactionalEmailJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("TransactionalEmailJob - execute " + jobExecutionContext);
        try {
            JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            String serverToken = (String) dataMap.get("serverToken");
            String senderSignature = (String) dataMap.get("senderSignature");
            String recipient = (String) dataMap.get("recipient");
            Integer templateId = (Integer) dataMap.get("templateId");
            String resetToken = (String) dataMap.get("resetToken");
            String passwordResetBaseUrl = (String) dataMap.get("passwordResetBaseUrl");

/*
            ApiClient client = Postmark.getApiClient(serverToken);

            TemplatedMessage message = new TemplatedMessage(senderSignature, recipient);
            message.setTemplateId(templateId);

            HashMap model = new HashMap<String, Object>();
            model.put("action_url_Value ", "http://localhost:8080/divroll/superusers/resetPassword?resetToken=" + resetToken);

            message.setTemplateModel(model);
            MessageResponse response = client.deliverMessage(message);
*/
            JSONObject templateModel = new JSONObject();
            templateModel.put("action_url", passwordResetBaseUrl + "resetToken=" + resetToken);

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
