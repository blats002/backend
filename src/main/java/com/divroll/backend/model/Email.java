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
package com.divroll.backend.model;

import org.json.JSONObject;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class Email {

  private String emailHost;
  private String emailPort;
  private String emailAddress;
  private String password;

  public String getEmailHost() {
    return emailHost;
  }

  public void setEmailHost(String emailHost) {
    this.emailHost = emailHost;
  }

  public String getEmailPort() {
    return emailPort;
  }

  public void setEmailPort(String emailPort) {
    this.emailPort = emailPort;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public JSONObject toJSONObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("emailHost", emailHost);
    jsonObject.put("emailPort", emailPort);
    jsonObject.put("emailAddress", emailAddress);
    jsonObject.put("password", password);
    return jsonObject;
  }

  public void fromJSONObject(JSONObject jsonObject) {
    if (jsonObject == null) {
      return;
    }
    setEmailHost(jsonObject.getString("emailHost"));
    setEmailPort(jsonObject.getString("emailPort"));
    setEmailAddress(jsonObject.getString("emailAddress"));
    setPassword(jsonObject.getString("password"));
  }
}
