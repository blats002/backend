package com.divroll.webdash.client.local.widgets.modals;

import com.divroll.webdash.client.local.events.AssetsEvents;
import com.divroll.webdash.client.local.events.activity.AssetsActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;

import javax.inject.Inject;

/**
 * Created by Hanan on 1/12/2016.
 */
public class MetaDataModal extends Composite {
    @Inject
    @DataField
    FormPanel panel;

    @Inject
    @DataField
    Button save;

    @Inject
    AssetsEvents events;


    @EventHandler("upload")
    public void upload(ClickEvent event){
        event.preventDefault();
        panel.submit();
    }

    public void submitComplete(){
        events.fireSubmittedEvent(new AssetsActivity());
    }

    public static native void toggle()/*-{
        var modal = $wnd.UIkit.modal("#uploadAsset");
        if ( modal.isActive() ) {
            modal.hide();
        } else {
            modal.show();
        }
    }-*/;

}
