package com.divroll.webdash.client.shared;

import java.io.Serializable;

public class MetaData implements Serializable {
    public MetaData(){}
    private String metaTitle;
    private String metaDescription;

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }
}
