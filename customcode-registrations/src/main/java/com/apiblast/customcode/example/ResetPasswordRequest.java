package com.apiblast.customcode.example;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/*
TemplateModel: {
            name: name,
            action_url: actionUrl,
            operating_system: browserOS,
            browser_name: browserName,
            support_url: "https://www.divroll.com/#support"
        }
*/
public class ResetPasswordRequest implements Serializable{
    @JsonProperty("name")
    private String name;
    @JsonProperty("action_url")
    private String action_url;
    @JsonProperty("operating_system")
    private String operating_system;
    @JsonProperty("browser_name")
    private String browser_name;
    @JsonProperty("support_url")
    private String support_url;
    public ResetPasswordRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction_url() {
        return action_url;
    }

    public void setAction_url(String action_url) {
        this.action_url = action_url;
    }

    public String getOperating_system() {
        return operating_system;
    }

    public void setOperating_system(String operating_system) {
        this.operating_system = operating_system;
    }

    public String getBrowser_name() {
        return browser_name;
    }

    public void setBrowser_name(String browser_name) {
        this.browser_name = browser_name;
    }

    public String getSupport_url() {
        return support_url;
    }

    public void setSupport_url(String support_url) {
        this.support_url = support_url;
    }
}
