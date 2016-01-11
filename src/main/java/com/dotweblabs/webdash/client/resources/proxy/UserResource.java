package com.divroll.webdash.client.resources.proxy;

import com.divroll.webdash.client.shared.User;
import com.google.gwt.core.client.GWT;
import org.restlet.client.resource.Result;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserResource {
    public void getUser(String userId, String token, final Result<User> result){
        final UserProxy resourceProxy = GWT.create(UserProxy.class);
        resourceProxy.getClientResource().setReference("/rest/users/" + userId);
        resourceProxy.getClientResource().addQueryParameter("token", token);
        resourceProxy.getUser(new Result<User>() {
            @Override
            public void onFailure(Throwable throwable) {
                result.onFailure(throwable);
            }
            @Override
            public void onSuccess(User user) {
                result.onSuccess(user);
            }
        });
    }
}
