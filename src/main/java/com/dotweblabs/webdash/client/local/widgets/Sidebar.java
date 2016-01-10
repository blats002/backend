package com.divroll.webdash.client.local.widgets;

import com.divroll.webdash.client.local.AssetsPage;
import com.divroll.webdash.client.local.BlogPage;
import com.divroll.webdash.client.local.DataPage;
import com.divroll.webdash.client.local.LoginPage;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import org.boon.di.In;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;


/**
 * Created by Hanan on 1/6/2016.
 */
@Templated
@Page
public class Sidebar extends Composite {

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
    Anchor logout;

    @Inject TransitionTo<AssetsPage> assets;
    @Inject TransitionTo<DataPage> Data;
    @Inject TransitionTo<BlogPage> blog;
    @Inject TransitionTo<LoginPage> Logout;


    @EventHandler ("asset")
    public void asset (ClickEvent event){
    event.preventDefault();
        assets.go();}

    @EventHandler ("blogs")
    public void blogs (ClickEvent event){
        event.preventDefault();
        blog.go();}

    @EventHandler ("data")
    public void data (ClickEvent event){
        event.preventDefault();
        Data.go();
    }

    @EventHandler ("logout")
    public void logout (ClickEvent event){
        event.preventDefault();
        Logout.go();
    }
}


