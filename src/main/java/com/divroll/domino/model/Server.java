package com.divroll.domino.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@XStreamAlias("server")
@ApiModel
public class Server {
    @ApiModelProperty
    private String name;
    @ApiModelProperty
    private String xodusRoot;
    @ApiModelProperty
    private String defaultUserStore;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXodusRoot() {
        return xodusRoot;
    }

    public void setXodusRoot(String xodusRoot) {
        this.xodusRoot = xodusRoot;
    }

    public String getdefaultUserStore() {
        return defaultUserStore;
    }

    public void setdefaultUserStore(String defaultUserStore) {
        this.defaultUserStore = defaultUserStore;
    }
}
