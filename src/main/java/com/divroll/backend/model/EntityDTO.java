package com.divroll.backend.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.wordnik.swagger.annotations.ApiModel;

import java.util.List;
import java.util.Map;

@XStreamAlias("entity")
@ApiModel
public class EntityDTO {

    private String entityType;
    private String entityId;
    @XStreamImplicit(itemFieldName = "aclRead")
    private List<EntityStub> aclRead;
    @XStreamImplicit(itemFieldName = "aclWrite")
    private List<EntityStub> aclWrite;
    @XStreamImplicit(itemFieldName = "roles")
    private List<EntityStub> roles;
    @XStreamImplicit(itemFieldName = "blobNames")
    private List<String> blobNames;
    @XStreamImplicit(itemFieldName = "linkNames")
    private List<String> linkNames;
    private Boolean publicRead;
    private Boolean publicWrite;
    private String dateCreated;
    private String dateUpdated;

    @XStreamImplicit(itemFieldName = "properties")
    private Map<String,Comparable> properties;

    public EntityDTO() {
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public List<EntityStub> getAclRead() {
        return aclRead;
    }

    public void setAclRead(List<EntityStub> aclRead) {
        this.aclRead = aclRead;
    }

    public List<EntityStub> getAclWrite() {
        return aclWrite;
    }

    public void setAclWrite(List<EntityStub> aclWrite) {
        this.aclWrite = aclWrite;
    }

    public List<EntityStub> getRoles() {
        return roles;
    }

    public void setRoles(List<EntityStub> roles) {
        this.roles = roles;
    }

    public List<String> getBlobNames() {
        return blobNames;
    }

    public void setBlobNames(List<String> blobNames) {
        this.blobNames = blobNames;
    }

    public List<String> getLinkNames() {
        return linkNames;
    }

    public void setLinkNames(List<String> linkNames) {
        this.linkNames = linkNames;
    }

    public Boolean getPublicRead() {
        return publicRead;
    }

    public void setPublicRead(Boolean publicRead) {
        this.publicRead = publicRead;
    }

    public Boolean getPublicWrite() {
        return publicWrite;
    }

    public void setPublicWrite(Boolean publicWrite) {
        this.publicWrite = publicWrite;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Map<String, Comparable> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Comparable> properties) {
        this.properties = properties;
    }
}
