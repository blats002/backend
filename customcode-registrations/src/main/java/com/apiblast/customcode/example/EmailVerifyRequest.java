package com.apiblast.customcode.example;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/*
				TemplateModel: {
				name: username,
				action_url: actionUrl,
				login_url: loginUrl,
				username: username,
				support_email: "support_email_Value",
				live_chat_url: "live_chat_url_Value",
				help_url: "help_url_Value",
				trial_length: "trial_length_Value",
				trial_start_date: "trial_start_date_Value",
				trial_end_date: "trial_end_date_Value"
        	}
			 */
public class EmailVerifyRequest implements Serializable {
    @JsonProperty("name")
    private String name;
    @JsonProperty("action_url")
    private String action_url;
    @JsonProperty("login_url")
    private String login_url;
    @JsonProperty("username")
    private String username;
    @JsonProperty("support_email")
    private String support_email;
    @JsonProperty("live_chat_url")
    private String live_chat_url;
    @JsonProperty("help_url")
    private String help_url;
    @JsonProperty("trial_length")
    private String trial_length;
    @JsonProperty("trial_start_date")
    private String trial_start_date;
    @JsonProperty("trial_end_date")
    private String trial_end_date;

    public EmailVerifyRequest() {}

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

    public String getLogin_url() {
        return login_url;
    }

    public void setLogin_url(String login_url) {
        this.login_url = login_url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSupport_email() {
        return support_email;
    }

    public void setSupport_email(String support_email) {
        this.support_email = support_email;
    }

    public String getLive_chat_url() {
        return live_chat_url;
    }

    public void setLive_chat_url(String live_chat_url) {
        this.live_chat_url = live_chat_url;
    }

    public String getHelp_url() {
        return help_url;
    }

    public void setHelp_url(String help_url) {
        this.help_url = help_url;
    }

    public String getTrial_length() {
        return trial_length;
    }

    public void setTrial_length(String trial_length) {
        this.trial_length = trial_length;
    }

    public String getTrial_start_date() {
        return trial_start_date;
    }

    public void setTrial_start_date(String trial_start_date) {
        this.trial_start_date = trial_start_date;
    }

    public String getTrial_end_date() {
        return trial_end_date;
    }

    public void setTrial_end_date(String trial_end_date) {
        this.trial_end_date = trial_end_date;
    }
}
