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
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@XStreamAlias("role")
@ApiModel
public class Role {

  private String entityId;
  private String name;

  @XStreamImplicit(itemFieldName = "aclRead")
  @ApiModelProperty(required = false, value = "")
  private List<EntityStub> aclRead;

  @XStreamImplicit(itemFieldName = "aclWrite")
  @ApiModelProperty(required = false, value = "")
  private List<EntityStub> aclWrite;

  @ApiModelProperty(required = false, value = "")
  private Boolean publicRead;

  @ApiModelProperty(required = false, value = "")
  private Boolean publicWrite;

  private String dateCreated;
  private String dateUpdated;

  public Role() {}

  public Role(String entityId) {
    setEntityId(entityId);
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
