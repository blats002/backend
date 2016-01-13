package com.divroll.webdash.client.local.widgets.modals;

import com.divroll.webdash.client.local.events.AssetsEvents;
import com.divroll.webdash.client.local.events.activity.AssetsActivity;
import com.divroll.webdash.client.shared.User;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.boon.di.In;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.*;

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


    @Bound
    @Inject
    @DataField
    Anchor fullName;

    @Bound
    @Inject
    @DataField
    TextBox username;

    @Bound
    @Inject
    @DataField
    TextBox password;

    @DataField
    Element role= DOM.createElement("select");


    @Inject
    @Model
   User model;


   @EventHandler("save")
    public void save(ClickEvent event){
        event.preventDefault();
       Window.alert("Storing: " + model.toString());
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
        return model;
    }

    @ModelSetter
    @Override
    public void setModel(User user) {
        model = user;
    }
}
