package com.divroll.domino.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@XStreamAlias("user")
@ApiModel
public class User {
    @ApiModelProperty(required = true, value = "Username")
    private String username;
    @ApiModelProperty(required = true, value = "Password")
    private String password;
    @ApiModelProperty(required = true, value = "Generated Authentication Token")
    private String webToken;

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
}
