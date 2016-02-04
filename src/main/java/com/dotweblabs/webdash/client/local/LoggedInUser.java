package com.divroll.webdash.client.local;

import com.divroll.webdash.client.shared.User;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LoggedInUser {

    private User user;
    private String token;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
