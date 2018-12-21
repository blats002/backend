package com.divroll.backend.model;

import com.divroll.backend.converters.CustomMapJSONConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.wordnik.swagger.annotations.ApiModel;

import java.util.LinkedList;
import java.util.List;

@XStreamAlias("link")
@ApiModel
public class Link {
    private String linkName;
    @XStreamImplicit(itemFieldName = "entities")
    private List<EntityStub> entities;

    public Link() {}

    public Link(String linkName, List<EntityStub> entities) {
        setLinkName(linkName);
        setEntities(entities);
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public List<EntityStub> getEntities() {
        if(entities == null) {
            entities = new LinkedList<EntityStub>();
        }
        return entities;
    }

    public void setEntities(List<EntityStub> entities) {
        this.entities = entities;
    }
}
