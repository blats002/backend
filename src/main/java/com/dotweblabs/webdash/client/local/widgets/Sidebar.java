package com.divroll.webdash.client.local.widgets;

import com.divroll.webdash.client.local.*;
import com.divroll.webdash.client.local.common.LoggedIn;
import com.divroll.webdash.client.shared.User;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import elemental.client.Browser;
import org.boon.di.In;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;


/**
 * Created by Hanan on 1/6/2016.
 */
@Templated
@Dependent
public class Sidebar extends Composite implements HasModel<User> {

    @Inject
    @DataField
    Anchor asset;

    @Inject
    @DataField
    Anchor blogs;

    @Inject
    @DataField
    Anchor data;

    @Inject
    @DataField
    Anchor domain;

    @Inject
    @DataField
    Anchor users;

    @Bound
    @DataField
    Element username = DOM.createElement("h3");

    @Inject
    @AutoBound
    private DataBinder<User> binder;

    @Inject TransitionTo<AssetsPage> assetsPage;
    @Inject TransitionTo<DataPage> dataPage;
    @Inject TransitionTo<BlogPage> blogPage;
    @Inject TransitionTo<LoginPage> loginPage;
    @Inject TransitionTo<DomainPage> domainPage;
    @Inject TransitionTo<UsersPage> usersPage;

    @EventHandler("asset")
    public void asset(ClickEvent event){
    event.preventDefault();
        event.preventDefault();
        assetsPage.go();
    }

    @EventHandler("blogs")
    public void blogs (ClickEvent event){
        event.preventDefault();
        blogPage.go();
    }

    @EventHandler("data")
    public void data(ClickEvent event){
        event.preventDefault();
        dataPage.go();
    }

    @EventHandler("domain")
    public void domain(ClickEvent event){
        event.preventDefault();
        domainPage.go();
    }

    @EventHandler("users")
    public void users(ClickEvent event){
        event.preventDefault();
        usersPage.go();
    }

//    private void hanleLoggedIn(@Observes @LoggedIn User user){
//        Browser.getWindow().getConsole().log("User logged in: " + user.getUsername());
//        setModel(user);
//    }

    @Override
    public User getModel() {
        return binder.getModel();
    }

    @Override
    public void setModel(User user) {
        binder.setModel(user);
    }
}


