package com.divroll.webdash.client.local;

import com.divroll.webdash.client.local.widgets.Navbar;
import com.divroll.webdash.client.local.widgets.Sidebar;
import com.divroll.webdash.client.local.widgets.modals.MetaDataModal;
import com.divroll.webdash.client.shared.Blog;
import com.divroll.webdash.client.shared.Value;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;


/**
 * Created by Hanan on 1/14/2016.
 */
@Templated("#maincontent")
@Dependent
@Page
public class EditBlogPage extends Composite /*implements HasModel<Blog>*/ {

    @Inject
    @DataField
    Navbar navbar;

    @Inject
    @DataField
    Sidebar menu;

    @Inject
    @DataField
    Button submit;

    @Inject
    @DataField
    Button cancel;

    @Inject
    @DataField
    MetaDataModal meta;

    @Inject
    @DataField
    Button logout;

    @Inject
    LoggedInUser loggedInUser;

    @PageShown
    public void ready(){
        menu.setModel(loggedInUser.getUser());
    }

    /*

    @Bound
    @DataField
    @Inject
    TextBox title;

    @Bound
    @Inject
    @DataField
    TextBox content;

    @Bound
    @Inject
    @DataField
    TextBox tags;

    @Bound
    @Inject
    @DataField
    TextBox published;

    @Bound
    @Inject
    @DataField
    TextBox author;

    @Inject
    @Model
    Blog model;
    
    @Inject
    TransitionTo<BlogPage> blogPage;

    @EventHandler("cancel")
    public void cancel(ClickEvent event) {
        event.preventDefault();
        blogPage.go();
    }


    @EventHandler("submit")
    public void submit(ClickEvent event){
        Window.alert("Storing: " + model.toString());
    }

    @Override
    public Blog getModel() {
        return model;
    }

    @ModelSetter
    @Override
    public void setModel(Blog blog) {
        model = blog;
    }
*/
}
