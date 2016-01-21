package com.divroll.webdash.client.local;

import com.divroll.webdash.client.resources.proxy.SubdomainsResource;
import com.divroll.webdash.client.shared.Subdomain;
import com.divroll.webdash.client.local.widgets.Navbar;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.Window;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.shared.api.annotations.*;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.restlet.client.resource.Result;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Templated("#content")
@Page
@Dependent
public class DomainPage extends Composite implements HasModel<Subdomain> {

    @Inject
    @AutoBound
    DataBinder<Subdomain> binder;

    @Inject
    @DataField
    Navbar navbar;

    @Inject
    @DataField
    Sidebar menu;

    @AutoBound
    @Inject
    @DataField
    TextBox subdomain;

    @Inject
    @DataField
    Button save;

    @Inject
    @DataField
    Button logout;

    @Inject
    LoggedInUser loggedInUser;

    @Inject
    SubdomainsResource subdomainsResource;

    @PageShown
    public void ready(){
        menu.setModel(loggedInUser.getUser());
    }

    @PostConstruct
    public void buildUI(){
    }

    @EventHandler("save")
    public void save(ClickEvent event){
        event.preventDefault();
        Window.alert("Subdomain: " + subdomain.getText());
        binder.getModel().setUserId(loggedInUser.getUser().getId());
        subdomainsResource.save(binder.getModel(), new Result<Subdomain>() {
            @Override
            public void onFailure(Throwable throwable) {
                Window.alert("ERROR saving domain");
            }

            @Override
            public void onSuccess(Subdomain subdomain) {
                Window.alert("SUCCESS!");
            }
        });
    }

    @Override
    public Subdomain getModel() {
        return binder.getModel();
    }

    @Override
    public void setModel(Subdomain subdomain) {
        binder.setModel(subdomain);
    }
}
