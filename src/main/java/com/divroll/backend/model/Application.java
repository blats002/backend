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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.json.JSONObject;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@XStreamAlias("application")
@ApiModel
public class Application {
  @ApiModelProperty(position = 0, required = false, value = "Auto-generated id")
  private String appId;

  @ApiModelProperty(required = true, value = "Generated Application Key")
  private String apiKey;

  @ApiModelProperty(required = true, value = "Generated Master Key")
  private String masterKey;

  @ApiModelProperty(required = true, value = "Unique Application Name")
  private String appName;

  @ApiModelProperty(required = false, value = "Email configuration")
  private Email emailConfig;

  @ApiModelProperty(required = false, value = "Cloud code")
  private String cloudCode;

  private String dateCreated;

  private String dateUpdated;

  private UserRoot user;

  private Superuser superuser;

  public String getId() {
    return appId;
  }

  public void setId(String id) {
    this.appId = id;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getMasterKey() {
    return masterKey;
  }

  public void setMasterKey(String masterKey) {
    this.masterKey = masterKey;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public Email getEmailConfig() {
    return emailConfig;
  }

  public void setEmailConfig(Email emailConfig) {
    this.emailConfig = emailConfig;
  }

  public UserRoot getUser() {
    return user;
  }

  public void setUser(UserRoot user) {
    this.user = user;
  }

  public String getCloudCode() {
    return this.cloudCode;
  }

  public void setCloudCode(String cloudCode) {
    this.cloudCode = cloudCode;
  }

  public String getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(String dateCreated) {
    this.dateCreated = dateCreated;
  }

  public String getDateUpdated() {
    return dateUpdated;
  }

  public void setDateUpdated(String dateUpdated) {
    this.dateUpdated = dateUpdated;
  }

  public Superuser getSuperuser() {
    return superuser;
  }

  public void setSuperuser(Superuser superuser) {
    this.superuser = superuser;
  }

  public JSONObject asJSONObject() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("appId", getAppId());
    jsonObject.put("apiKey", getApiKey());
    jsonObject.put("appName", getAppName());
    jsonObject.put("cloudCode", getCloudCode());
    jsonObject.put("dateCreated", getDateCreated());
    jsonObject.put("dateUpdated", getDateUpdated());
    return jsonObject;
  }
}
