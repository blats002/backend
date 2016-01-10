package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.common.Submitted;
import com.divroll.webdash.client.local.events.Form;
import com.divroll.webdash.client.local.widgets.Footer;
import com.divroll.webdash.client.local.widgets.Navbar;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.divroll.webdash.client.local.widgets.modals.UploadWebsiteModal;
import com.divroll.webdash.client.resources.js.Resources;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;

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

    @PageShown
    public void ready(){

    }

    public void renderTable(@Observes @Submitted Form payload){
        Window.alert("Rendering Table: " + payload);
    }

}


