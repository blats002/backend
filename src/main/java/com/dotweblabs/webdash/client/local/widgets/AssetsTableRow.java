package com.divroll.webdash.client.local.widgets;

import com.divroll.webdash.client.shared.File;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
@Templated
public class AssetsTableRow extends Composite implements HasModel<File> {
    //@Bound
//    @Inject
//    @DataField
//    Anchor fileName;

    //@Bound
//    @DataField
//    Element modified = DOM.createElement("td");

//    @DataField
//    Element type = DOM.createElement("td");

//    @Inject
    //@Model
    File model;

    @Override
    public File getModel() {
        return model;
    }

    //@ModelSetter
    @Override
    public void setModel(File file) {
        model = file;
    }
}
