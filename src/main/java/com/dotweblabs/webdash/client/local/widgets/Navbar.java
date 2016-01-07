package com.divroll.webdash.client.local.widgets;

import com.divroll.webdash.client.local.SettingPage;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.inject.Inject;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.Dependent;

/**
 * Created by Hanan on 1/6/2016.
 */
@Dependent
@Templated
public class NavBar extends Composite {

    @Inject
    @DataField
    Anchor settings;

    @Inject
    TransitionTo<SettingPage> Setting;

    @EventHandler ("settings")
    public void settings(ClickEvent event){
        event.preventDefault();
        Setting.go();
    }
}
