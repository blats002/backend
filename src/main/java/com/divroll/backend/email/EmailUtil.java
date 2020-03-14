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
package com.divroll.backend.email;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;

// import javax.mail.Message;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EmailUtil {

  private static final Logger LOG = LoggerFactory.getLogger(EmailUtil.class);

  /**
   * Utility method to send simple HTML email
   *
   * @param session
   * @param toEmail
   * @param subject
   * @param body
   */
  public static void sendEmail(
      Session session,
      String toEmail,
      String fromEmail,
      String fromName,
      String subject,
      String body)
      throws MessagingException, UnsupportedEncodingException {
    LOG.info("*** Sending email via SMTP ***");

    MimeMessage msg = new MimeMessage(session);
    // set message headers
    msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
    msg.addHeader("format", "flowed");
    msg.addHeader("Content-Transfer-Encoding", "8bit");

    msg.setFrom(new InternetAddress(fromEmail, fromName));
    msg.setReplyTo(InternetAddress.parse(fromEmail, false));
    msg.setSubject(subject, "UTF-8");
    msg.setText(body, "UTF-8");
    msg.setSentDate(new Date());
    msg.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
    LOG.info("Message is ready");

    Transport.send(msg);
    LOG.info("EMail Sent Successfully");
    //        LOG.info("*** Sending email via Postmark ***");
    //        ApiClient client = Postmark.getApiClient("");
    //        Message message = new Message("",
    // "", "Hello from Postmark!", "Hello message body");
    //        try {
    //            MessageResponse response = client.deliverMessage(message);
    //        } catch (PostmarkException e) {
    //            e.printStackTrace();
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }

  }
}
