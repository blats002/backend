package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.widgets.Navbar;
import com.divroll.webdash.client.local.widgets.NavBar;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.Dependent;

/**
 * Created by Hanan on 1/6/2016.
 */
@Templated("#content")
@Page
@Dependent
public class NewBlogPage extends Composite {

    @Inject
    @DataField
    Button metadata;

    @Inject
    @DataField
    Button submit;

    @Inject
    @DataField
    Button cancel;

    @Inject
    @DataField
    Button preview;

    @Inject
    @DataField
    Navbar navbar;

    @Inject
    @DataField
    Sidebar menu;


    @Inject
    TransitionTo<BlogPage> Blog;

    @EventHandler ("cancel")
    public void cancel(ClickEvent event){
        event.preventDefault();
        Blog.go();
    }

}
