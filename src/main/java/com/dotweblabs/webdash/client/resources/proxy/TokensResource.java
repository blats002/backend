package com.divroll.webdash.client.resources.proxy;

import com.google.gwt.core.client.GWT;
import org.restlet.client.resource.Result;
import org.restlet.client.data.ChallengeResponse;
import org.restlet.client.data.ChallengeScheme;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokensResource {
    public void signin(String username, String password, final Result<String> result){
        final TokensProxy resourceProxy = GWT.create(TokensProxy.class);
        resourceProxy.getClientResource().setReference("/rest/tokens");
        ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, username, password);
        resourceProxy.getClientResource().setChallengeResponse(cr);
        resourceProxy.signin(new Result<String>() {
            @Override
            public void onFailure(Throwable throwable) {
                result.onFailure(throwable);
            }

            @Override
            public void onSuccess(String s) {
                result.onSuccess(s);
            }
        });
    }
}
