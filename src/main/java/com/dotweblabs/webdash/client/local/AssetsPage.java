package com.divroll.webdash.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.eclipse.xtend.lib.annotations.Data;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Hanan on 1/5/2016.
 */
@Templated("#content")
@Page
@Dependent
public class AssetsPage extends Composite{

    @Inject
    @DataField
    Button logout;

    @Inject
    TransitionTo<LoginPage> Login;

    @EventHandler ("logout")
    public void logout(ClickEvent event){
        Login.go();
    }
}
