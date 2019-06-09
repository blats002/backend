/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019, Divroll, and individual contributors
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
package com.divroll.backend.service.jee;

import com.divroll.backend.email.EmailConfig;
import com.divroll.backend.email.EmailUtil;
import com.divroll.backend.service.EmailService;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */

/**
 * Outgoing Mail (EmailConfig) Server requires TLS or SSL: smtp.gmail.com (use authentication) Use
 * Authentication: Yes Port for TLS/STARTTLS: 587
 */
public class JeeEmailService implements EmailService {

  private static final Logger LOG = LoggerFactory.getLogger(JeeEmailService.class);

  @Override
  public void sendEmail(
      EmailConfig config, String from, String password, String to, String subject, String htmlBody)
      throws UnsupportedEncodingException, MessagingException {
    LOG.info("Sending email...");

    final String fromEmail = from;
    final String toEmail = to;

    LOG.info("TLSEmail Start");
    Properties props = new Properties();
    props.put("mail.smtp.host", config.getSmtpHost()); // EmailConfig Host
    props.put("mail.smtp.port", config.getTlsPort()); // TLS Port
    props.put("mail.smtp.auth", "true"); // enable authentication
    props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS

    LOG.with(props).info("Logging email properties");

    // create Authenticator object to pass in Session.getInstance argument
    Authenticator auth =
        new Authenticator() {
          // override the getPasswordAuthentication method
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(fromEmail, password);
          }
        };
    Session session = Session.getInstance(props, auth);

    EmailUtil.sendEmail(session, toEmail, fromEmail, fromEmail, subject, htmlBody);
  }
}
