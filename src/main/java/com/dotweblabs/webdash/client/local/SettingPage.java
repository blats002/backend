package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.widgets.Footer;
import com.divroll.webdash.client.local.widgets.NavBar;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import javafx.geometry.Side;
import org.boon.di.In;
import org.eclipse.xtend.lib.annotations.Data;
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
public class SettingPage extends Composite {

    @Inject
    @DataField
    Sidebar menu;

}
