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
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@XStreamAlias("user")
@ApiModel
public class User {
  @ApiModelProperty(required = false, value = "Entity Id")
  private String entityId;

  @ApiModelProperty(required = true, value = "Username")
  private String username;

  @ApiModelProperty(required = true, value = "Password")
  private String password;

  @ApiModelProperty(required = true, value = "Email")
  private String email;

  @ApiModelProperty(required = false, value = "Generated Authentication Token")
  private String authToken;

  @ApiModelProperty(required = false)
  @XStreamImplicit(itemFieldName = "roles")
  private List<Role> roles;

  @XStreamImplicit(itemFieldName = "aclRead")
  @ApiModelProperty(required = false, value = "")
  private List<EntityStub> aclRead;

  @XStreamImplicit(itemFieldName = "aclWrite")
  @ApiModelProperty(required = false, value = "")
  private List<EntityStub> aclWrite;

  @XStreamImplicit(itemFieldName = "blobNames")
  @ApiModelProperty(required = false, value = "")
  private List<String> blobNames;

  @ApiModelProperty(required = false, value = "")
  private Boolean publicRead;

  @ApiModelProperty(required = false, value = "")
  private Boolean publicWrite;

  private String dateCreated;
  private String dateUpdated;

  @XStreamImplicit(itemFieldName = "links")
  private List<Link> links;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public List<Role> getRoles() {
    if (roles == null) {
      roles = new LinkedList<Role>();
    }
    return roles;
  }

  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }

  public List<EntityStub> getAclRead() {
    return aclRead;
  }

  public void setAclRead(List<EntityStub> aclRead) {
    this.aclRead = aclRead;
  }

  public List<EntityStub> getAclWrite() {
    return aclWrite;
  }

  public void setAclWrite(List<EntityStub> aclWrite) {
    this.aclWrite = aclWrite;
  }

  public Boolean getPublicRead() {
    return publicRead;
  }

  public void setPublicRead(Boolean publicRead) {
    this.publicRead = publicRead;
  }

  public Boolean getPublicWrite() {
    return publicWrite;
  }

  public void setPublicWrite(Boolean publicWrite) {
    this.publicWrite = publicWrite;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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

  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }

  public List<String> getBlobNames() {
    return blobNames;
  }

  public void setBlobNames(List<String> blobNames) {
    this.blobNames = blobNames;
  }
}
