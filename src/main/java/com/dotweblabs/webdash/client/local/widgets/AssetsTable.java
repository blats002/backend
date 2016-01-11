package com.divroll.webdash.client.local.widgets;

import com.divroll.webdash.client.shared.File;
import com.divroll.webdash.client.shared.Files;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Dependent
@Templated
public class AssetsTable extends Composite implements HasModel<Files> {

    @Inject
    Instance<AssetsTableRow> tableRowInstance;

    @DataField
    Element rows = DOM.createElement("tbody");

    Files model;

    @PostConstruct
    public void buildUI(){
        rows.removeAllChildren();
    }

    @Override
    public Files getModel() {
        return model;
    }

    @Override
    public void setModel(Files files) {
        this.model = files;
        for(File file : files.getList()){
            AssetsTableRow tr = tableRowInstance.get();
            tr.setModel(file);
            rows.appendChild(tr.getElement());
        }
    }
}
