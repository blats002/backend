package com.divroll.webdash.client.local.widgets.modals;

import com.divroll.webdash.client.local.events.AssetsEvents;
import com.divroll.webdash.client.local.events.activity.AssetsActivity;
import com.divroll.webdash.client.resources.js.Resources;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@ApplicationScoped
@Templated
public class UploadWebsiteModal extends Composite {

    @Inject
    @DataField
    FormPanel panel;

    @Inject
    @DataField
    Button upload;

    @Inject
    AssetsEvents events;

    @PostConstruct
    public void buildUI(){
        panel.setAction("/rest/uploads?upload_type=assets_zip");
        panel.setMethod(FormPanel.METHOD_POST);
        panel.setEncoding(FormPanel.ENCODING_MULTIPART);
        final FileUpload upload = new FileUpload();
        upload.setName("uploadFormElement");
        panel.add(upload);
        panel.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent submitEvent) {

            }
        });
        panel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent submitCompleteEvent) {
                submitComplete();
                toggle();
            }
        });
    }

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
