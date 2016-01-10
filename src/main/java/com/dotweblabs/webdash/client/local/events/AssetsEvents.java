package com.divroll.webdash.client.local.events;

import com.divroll.webdash.client.local.common.Submitted;
import com.divroll.webdash.client.shared.Files;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class AssetsEvents {
    @Inject
    @Submitted
    Event<Form> submitted;

    public void fireSubmittedEvent(Form payload){
        submitted.fire(payload);
    }
}
