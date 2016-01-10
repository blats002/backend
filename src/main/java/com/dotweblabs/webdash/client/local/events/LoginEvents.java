package com.divroll.webdash.client.local.events;

import com.divroll.webdash.client.local.common.LoggedIn;
import com.divroll.webdash.client.local.common.LoggedOut;
import com.divroll.webdash.client.shared.Token;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class LoginEvents {

    @Inject
    @LoggedIn
    Event<Token> loggedIn;

    @Inject
    @LoggedOut
    Event<Token> loggedOut;

    public void fireSocialLoginEvent(Token accessToken){
        loggedIn.fire(accessToken);
    }

    public void fireSocialLogoutEvent(){
        Token dummy = new Token();
        loggedOut.fire(dummy);
    }

}
