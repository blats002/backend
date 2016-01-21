package com.divroll.webdash.client.local.events;

import com.divroll.webdash.client.local.common.LoggedIn;
import com.divroll.webdash.client.local.common.LoggedOut;
import com.divroll.webdash.client.shared.Token;
import com.divroll.webdash.client.shared.User;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class LoginEvents {

    @Inject
    @LoggedIn
    Event<User> loggedIn;

    @Inject
    @LoggedOut
    Event<User> loggedOut;

    public void fireLoginEvent(User user){
        loggedIn.fire(user);
    }

}
