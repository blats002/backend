package com.divroll.webdash.client.resources.proxy;

import com.divroll.webdash.client.shared.Subdomain;
import com.divroll.webdash.client.shared.Subdomains;
import com.google.gwt.core.client.GWT;
import org.restlet.client.data.Method;
import org.restlet.client.resource.Result;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SubdomainsResource {
    public void save(Subdomain subdomain, final Result<Subdomain> result){
        final SubdomainsProxy resourceProxy = GWT.create(SubdomainsProxy.class);
        resourceProxy.getClientResource().setReference("/rest/subdomains");
        resourceProxy.save(subdomain, new Result<Subdomain>() {
            @Override
            public void onFailure(Throwable throwable) {
                result.onFailure(throwable);
            }
            @Override
            public void onSuccess(Subdomain subdomain) {
                result.onSuccess(subdomain);
            }
        });
    }
    public void list(Result<Subdomains> result){

    }
}
