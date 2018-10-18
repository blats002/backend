package com.divroll.backend.trigger;

public class TriggerResponse {
    private boolean success;
    private String body;
    public void success() {
        this.success = true;
    }
    public void error() {
        this.success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
