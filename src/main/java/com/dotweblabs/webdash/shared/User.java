/**
 *
 * Copyright (c) 2016 Divroll and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.divroll.webdash.shared;

import com.hunchee.twist.annotations.Entity;
import com.hunchee.twist.annotations.Id;
import org.jboss.errai.databinding.client.api.Bindable;

import java.io.Serializable;
import java.util.Date;

@Entity
@Bindable
public class User implements Serializable {
    @Id
    private Long id;
    private String email;
    private String username;
    private String password;
    private String fullName;
    private Date created;
    private Date modified;

    public User(){}

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Override
    public String toString() {
        String s = "";
        s += "Username=" + username + "\n"
                + "Password=" + password + "\n"
                + "Fullname=" + fullName + "\n";
        return s;
    }
}
