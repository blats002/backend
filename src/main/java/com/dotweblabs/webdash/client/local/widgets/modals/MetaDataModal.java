package com.divroll.webdash.client.local.widgets.modals;

import com.divroll.webdash.client.local.events.AssetsEvents;
import com.divroll.webdash.client.local.events.activity.AssetsActivity;
import com.divroll.webdash.client.shared.Blog;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.*;
import com.google.gwt.user.client.Window;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
@Templated
public class MetaDataModal extends Composite implements HasModel<Blog> {

    @Inject
    @DataField
    Button save;

    @Bound
    @Inject
    @DataField
    TextBox metaTitle;

    @Bound
    @Inject
    @DataField
    TextBox metaDescription;

    @Model
    @Inject
    Blog model;

    @EventHandler("save")
    public void save(ClickEvent event){
        event.preventDefault();
        Window.alert("Title: " + metaTitle.getText() + "\n" + "Description: " + metaDescription.getText());
        toggle();
    }

    public static native void toggle()/*-{
        var modal = $wnd.UIkit.modal("#meta");
        if ( modal.isActive() ) {
            modal.hide();
        } else {
            modal.show();
        }
    }-*/;

    @Override
    public Blog getModel() {
        return model;
    }

    @ModelSetter
    @Override
    public void setModel(Blog blog) {
        this.model = blog;
    }
}
