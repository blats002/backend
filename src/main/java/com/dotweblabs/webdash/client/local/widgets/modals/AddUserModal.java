package com.divroll.webdash.client.local.widgets.modals;

import com.divroll.webdash.client.local.events.AssetsEvents;
import com.divroll.webdash.client.local.events.activity.AssetsActivity;
import com.divroll.webdash.client.shared.User;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
import org.boon.di.In;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;

import javax.inject.Inject;

/**
 * Created by Hanan on 1/12/2016.
 */

public class AddUserModal extends Composite implements HasModel<User> {

    @Inject
    @DataField
    FormPanel panel;

    @Inject
    @DataField
    Button save;

    @Inject
    AssetsEvents events; 

    @Inject
    @DataField
    Anchor fullname;
    
    @Inject
    @DataField
    TextBox username;
    
    @Inject
    @DataField
    TextBox password;
    
    @DataField
    Element role= DOM.createElement("select");

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

    @Override
    public User getModel() {
        return null;
    }

    @Override
    public void setModel(User user) {

    }
}
