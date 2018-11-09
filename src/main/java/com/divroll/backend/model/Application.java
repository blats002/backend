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
package com.divroll.backend.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

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

  private String appName;

  @ApiModelProperty(required = false, value = "Email configuration")
  private Email emailConfig;

  @ApiModelProperty(required = false, value = "Cloud code")
  private String cloudCode;

  private String dateCreated;
  private String dateUpdated;

  private UserRootDTO user;

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

  public UserRootDTO getUser() {
    return user;
  }

  public void setUser(UserRootDTO user) {
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
}
