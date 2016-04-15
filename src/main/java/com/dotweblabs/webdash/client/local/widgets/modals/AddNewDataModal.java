package com.divroll.webdash.client.local.widgets.modals;

import com.divroll.webdash.client.local.events.AssetsEvents;
import com.divroll.webdash.client.local.events.activity.AssetsActivity;
import com.divroll.webdash.client.shared.Value;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import org.boon.di.In;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.context.Dependent;

/**
 * Created by Hanan on 1/12/2016.
 */
@Dependent
@Templated
public class AddNewDataModal extends Composite implements HasModel<Value> {

    @Inject
    @AutoBound
    private DataBinder<Value> dataBinder;

    @Inject
    @DataField
    FormPanel panel;

    @Inject
    @DataField
    Button save;

    @Bound
    @Inject
    @DataField
    TextBox type;

    @AutoBound
    @Inject
    @DataField
    TextBox value;

    @Inject
    @Model
    Value model;

    @EventHandler("save")
    public void save(ClickEvent event) {
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
    public Value getModel() {
        return null;
    }

    @ModelSetter
    @Override
    public void setModel(Value value) {
        model = value;


    }

}