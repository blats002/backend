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
    @ApiModelProperty(required = false, value = "Generated Authentication Token")
    private String webToken;
    @ApiModelProperty(required = false)
    @XStreamImplicit(itemFieldName = "roles")
    private List<Role> roles;


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

    public String getWebToken() {
        return webToken;
    }

    public void setWebToken(String webToken) {
        this.webToken = webToken;
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
}