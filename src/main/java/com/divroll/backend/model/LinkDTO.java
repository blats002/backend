package com.divroll.backend.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class LinkDTO implements Serializable {
    private String linkName;
    @XStreamImplicit(itemFieldName = "entities")
    private List<EntityDTO> entities;

    public LinkDTO() {}

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public List<EntityDTO> getEntities() {
        if(entities == null) {
            entities = new LinkedList<>();
        }
        return entities;
    }

    public void setEntities(List<EntityDTO> entities) {
        this.entities = entities;
    }
}
