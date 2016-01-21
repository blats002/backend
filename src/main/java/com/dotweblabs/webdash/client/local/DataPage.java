package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.widgets.Footer;
import com.divroll.webdash.client.local.widgets.Navbar;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.divroll.webdash.client.local.widgets.modals.AddNewDataModal;
import com.divroll.webdash.client.local.widgets.modals.EditDataModal;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import org.eclipse.xtend.lib.annotations.Data;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.Dependent;

/**
 * Created by Hanan on 1/6/2016.
 */
@Templated("#content")
@Page
@Dependent
public class DataPage extends Composite {

    @Inject
    @DataField
    Navbar navbar;

    @Inject
    @DataField
    Sidebar menu;

    @Inject
    @DataField
    AddNewDataModal dataModal;

    @Inject
    @DataField
    EditDataModal editData;

    @Inject
    LoggedInUser loggedInUser;

    @PageShown
    public void ready(){
        menu.setModel(loggedInUser.getUser());
    }

}
