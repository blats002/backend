package com.divroll.webdash.client.resources.proxy;

import com.divroll.webdash.client.shared.Token;
import com.google.gwt.core.client.GWT;
//import io.reinert.gdeferred.DoneCallback;
//import io.reinert.requestor.Requestor;
//import io.reinert.requestor.auth.BasicAuth;
import elemental.client.Browser;
import org.restlet.client.data.MediaType;
import org.restlet.client.resource.Result;
import org.restlet.client.data.ChallengeResponse;
import org.restlet.client.data.ChallengeScheme;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokensResource {
    public void signin(String username, String password, final Result<Token> result){
        final TokensProxy resourceProxy = GWT.create(TokensProxy.class);
        resourceProxy.getClientResource().setReference("/rest/tokens");
        ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, username, password);
        resourceProxy.getClientResource().setChallengeResponse(cr);
//        resourceProxy.getClientResource().accept(MediaType.APPLICATION_JSON);
        resourceProxy.signin(new Result<Token>() {
            @Override
            public void onFailure(Throwable throwable) {
                String error = throwable.getCause().getMessage();
                Browser.getWindow().getConsole().log(error);
                result.onFailure(throwable);
            }
            @Override
            public void onSuccess(Token s) {
                result.onSuccess(s);
            }
        });
    }
}
