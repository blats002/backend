package com.divroll.backend.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@XStreamAlias("function")
@ApiModel
public class Function {

    @ApiModelProperty(position = 0, required = false, value = "Application Id")
    private String appId;
    @ApiModelProperty(position = 1, required = true, value = "Unique function name")
    private String functionName;
    @ApiModelProperty(position = 2, required = false, value = "Base64 encoded Jar file")
    private String jar;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getJar() {
        return jar;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

}
