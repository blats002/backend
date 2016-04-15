package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.events.LoginEvents;
import com.divroll.webdash.client.local.widgets.Footer;
import com.divroll.webdash.client.local.widgets.Navbar;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import javax.inject.Inject;
import org.eclipse.xtend.lib.annotations.Data;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.Dependent;

/**
 * Created by Hanan on 1/5/2016.
 */
@Templated("#content")
@Page
@Dependent
public class BlogPage extends Composite{

    @Inject
    @DataField
    Navbar navbar;

    @Inject
    @DataField
    Sidebar menu;

    @Inject
    @DataField
    Anchor newBlog;

    @Inject
    @DataField
    Anchor editBlog; // TODO: Add the Table instead

    @Inject TransitionTo<EditBlogPage> editBlogPage;
    @Inject TransitionTo <NewBlogPage> newBlogPage;

    @Inject
    @DataField
    Button logout;

    @Inject
    LoginEvents loginEvents;

    @Inject
    LoggedInUser loggedInUser;

    @Inject
    TransitionTo<LoginPage> loginPage;

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

    @EventHandler("newBlog")
    public void add(ClickEvent event) {
        event.preventDefault();
        newBlogPage.go();
    }
    @EventHandler("editBlog")
    public void next(ClickEvent event){
        event.preventDefault();
        editBlogPage.go();
    }
}


