package com.divroll.webdash.shared;

import java.io.Serializable;

public class Token implements Serializable {

    private String token;
    private Long userId;

    public Token(){}

    public Token(Long userId, String token){
        this.userId = userId;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
