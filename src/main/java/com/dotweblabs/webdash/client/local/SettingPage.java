package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.events.LoginEvents;
import com.divroll.webdash.client.local.widgets.Footer;
import com.divroll.webdash.client.local.widgets.Navbar;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Hanan on 1/6/2016.
 */

@Templated("#content")
@Page
@Dependent
public class SettingPage extends Composite {

    @Inject
    @DataField
    Navbar navbar;

    @Inject
    @DataField
    Sidebar menu;

    @Inject
    LoggedInUser loggedInUser;

    @Inject
    LoginEvents loginEvents;

    @Inject
    TransitionTo<LoginPage> loginPage;

    @Inject
    @DataField
    Button logout;

    @PageShown
    public void ready(){
        menu.setModel(loggedInUser.getUser());
    }

    @EventHandler("logout")
    public void logout(ClickEvent event){
        event.preventDefault();
        loggedInUser.setUser(null);
        loginEvents.fireLogoutEvent(null);
        Multimap<String, String> state = ArrayListMultimap.create();
        state.put("logout", "true");
        loginPage.go(state);
    }

}
