package com.divroll.webdash.client.local;

import com.divroll.webdash.client.shared.User;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LoggedInUser {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
