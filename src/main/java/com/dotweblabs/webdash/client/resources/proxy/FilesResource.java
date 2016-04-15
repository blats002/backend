package com.divroll.webdash.client.resources.proxy;

import com.divroll.webdash.client.shared.Files;
import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import org.restlet.client.resource.Result;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FilesResource {
    public void list(final Result<Files> result){
        final FilesProxy resourceProxy = GWT.create(FilesProxy.class);
        resourceProxy.getClientResource().setReference("/rest/files");
        resourceProxy.list(new Result<Files>() {
            @Override
            public void onFailure(Throwable throwable) {
                Browser.getWindow().getConsole().log(throwable.getLocalizedMessage());
                result.onFailure(throwable);
            }
            @Override
            public void onSuccess(Files files) {
                result.onSuccess(files);
            }
        });
    }
}
