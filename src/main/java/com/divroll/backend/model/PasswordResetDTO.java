package com.divroll.backend.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@XStreamAlias("user")
@ApiModel
public class PasswordResetDTO {
    @ApiModelProperty(required = true, value = "Username")
    private String username;
    @ApiModelProperty(required = true, value = "Password")
    private String password;
    @ApiModelProperty(required = true, value = "New Password")
    private String newPassword;

    public PasswordResetDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
