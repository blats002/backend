package com.divroll.webdash.client.local.widgets.modals;

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
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
@Templated
public class UploadWebsiteModal extends Composite {

    @Inject
    @DataField
    FormPanel panel;

//    @PageShown
//    public void ready(){
//        ScriptInjector.fromString(Resources.INSTANCE.assetsJS().getText())
//                .setWindow(ScriptInjector.TOP_WINDOW).inject();
//    }

    @PostConstruct
    public void buildUI(){
        panel.setAction("/rest/uploads?upload_type=assets_zip");
        final FileUpload upload = new FileUpload();
        upload.setName("uploadFormElement");
        panel.add(upload);
//        panel.add(new Button("Submit", new ClickHandler() {
//            public void onClick(ClickEvent event) {
//                panel.submit();
//            }
//        }));
        panel.addSubmitHandler(new FormPanel.SubmitHandler() {
            @Override
            public void onSubmit(FormPanel.SubmitEvent submitEvent) {

            }
        });
        panel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent submitCompleteEvent) {

            }
        });
    }

}
