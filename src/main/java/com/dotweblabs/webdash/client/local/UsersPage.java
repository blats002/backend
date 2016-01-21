package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.widgets.Navbar;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.divroll.webdash.client.local.widgets.modals.AddUserModal;
import com.divroll.webdash.client.local.widgets.modals.EditUserModal;
import com.divroll.webdash.client.shared.User;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Templated("#content")
@Page
@Dependent
public class UsersPage extends Composite {

    @Inject
    @DataField
    Navbar navbar;

    @Inject
    @DataField
    Sidebar menu;

    @Inject
    @DataField
    AddUserModal addUser;

    @Inject
    @DataField
    EditUserModal editUser;

    @Inject
    LoggedInUser loggedInUser;

    @Inject
    @DataField
    Button logout;

    @PageShown
    public void ready(){
        menu.setModel(loggedInUser.getUser());
    }

}
