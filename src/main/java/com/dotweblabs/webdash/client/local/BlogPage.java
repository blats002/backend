package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.widgets.Footer;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import org.boon.Ok;
import org.eclipse.xtend.lib.annotations.Data;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
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

}
