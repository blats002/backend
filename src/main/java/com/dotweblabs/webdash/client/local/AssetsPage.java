package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.events.activity.AssetsActivity;
import com.divroll.webdash.client.local.common.Submitted;
import com.divroll.webdash.client.local.widgets.*;
import com.divroll.webdash.client.local.widgets.modals.UploadWebsiteModal;
import com.divroll.webdash.client.resources.proxy.FilesProxy;
import com.divroll.webdash.client.resources.proxy.FilesResource;
import com.divroll.webdash.client.shared.File;
import com.divroll.webdash.client.shared.Files;
import com.divroll.webdash.client.resources.js.Resources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.DOM;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import elemental.client.Browser;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.restlet.client.resource.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hanan on 1/5/2016.
 */
@Templated("#content")
@ApplicationScoped
@Page
public class AssetsPage extends Composite {

    @Inject
    @DataField
    Navbar navbar;

    @Inject
    @DataField
    Sidebar menu;

    @Inject
    @DataField
    Button logout;

    @Inject
    @DataField
    UploadWebsiteModal uploadAsset;

    @Inject
    @DataField
    AssetsTable table;

    @Inject
    FilesResource filesResource;

    @Inject
    LoggedInUser loggedInUser;

    @PageShown
    public void ready(){
        UIkit.notify("Welcome " + loggedInUser.getUser().getUsername());
    }

    public void renderTable(@Observes @Submitted AssetsActivity payload){
//        Window.alert("Rendering Table: " + payload);
//        Files files = new Files();
//        List<File> list = new ArrayList<File>();
//        list.add(new File("test1/test.txt"));
//        list.add(new File("test1/test2.txt"));
//        files.setList(list);

        filesResource.list(new Result<Files>() {
            @Override
            public void onFailure(Throwable throwable) {
                Browser.getWindow().getConsole().log(throwable.getLocalizedMessage());
            }
            @Override
            public void onSuccess(Files files) {
                table.setModel(files);
            }
        });


//        final FilesProxy resourceProxy = GWT.create(FilesProxy.class);
//        resourceProxy.getClientResource().setReference("/rest/files");
//        resourceProxy.list(new Result<Files>() {
//            @Override
//            public void onFailure(Throwable throwable) {
//                Browser.getWindow().getConsole().log(throwable.getLocalizedMessage());
//            }
//            @Override
//            public void onSuccess(Files files) {
//                table.setModel(files);
//            }
//        });

    }

}


