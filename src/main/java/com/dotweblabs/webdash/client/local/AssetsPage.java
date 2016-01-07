package com.divroll.webdash.client.local;


import com.divroll.webdash.client.local.widgets.Sidebar;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import javax.enterprise.context.Dependent;

/**
 * Created by Hanan on 1/5/2016.
 */

@Templated("#content")
@Dependent
@Page
public class AssetsPage extends Composite{

    @Inject
    @DataField
    Sidebar menu;
    }


