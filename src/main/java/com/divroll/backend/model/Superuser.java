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
 */
package com.divroll.backend.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@XStreamAlias("superuser")
@ApiModel
public class Superuser {

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

    private Boolean active;


    public Superuser() {}

    public Superuser(String username, String password) {
        setUsername(username);
        setPassword(password);
    }

    public Superuser(String username, String password, String email) {
        setUsername(username);
        setPassword(password);
        setEmail(email);
    }

    public Superuser(String username, String password, String email, Boolean active) {
        setUsername(username);
        setPassword(password);
        setEmail(email);
        setActive(active);
    }


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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
