package com.divroll.backend.model;

import com.divroll.backend.converters.CustomMapJSONConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.wordnik.swagger.annotations.ApiModel;

import java.util.List;
import java.util.Map;

@XStreamAlias("link")
@ApiModel
public class Link {
    private String linkName;
    @XStreamImplicit(itemFieldName = "entities")
    private List<CustomHashMap<String,Comparable>> entities;

    public Link() {}

    public Link(String linkName, List<CustomHashMap<String,Comparable>> entities) {
        setLinkName(linkName);
        setEntities(entities);
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public List<CustomHashMap<String,Comparable>> getEntities() {
        return entities;
    }

    public void setEntities(List<CustomHashMap<String,Comparable>> entities) {
        this.entities = entities;
    }
}
