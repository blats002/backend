package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.widgets.Footer;
import com.divroll.webdash.client.local.widgets.NavBar;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import org.boon.Ok;
import org.boon.di.In;
import org.eclipse.xtend.lib.annotations.Data;
import org.jboss.errai.ui.nav.client.local.Page;
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
    Sidebar menu;


    @Inject
    @DataField
    Button saveBlog;

    @Inject
    @DataField
    Button cancel;

    @Inject
    @DataField
    Button yes;

    @Inject
    @DataField
    Anchor Add;

    @Inject
    TransitionTo<NewBlogPage> New;

    @EventHandler ("Add")
    public void Add(ClickEvent event){
        event.preventDefault();
        New.go();
    }

}
