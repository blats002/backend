package com.divroll.webdash.client.shared;

import com.hunchee.twist.annotations.Entity;
import com.hunchee.twist.annotations.Id;

import java.io.Serializable;

@Entity
public class Subdomain implements Serializable {
    @Id
    private Long id;
    private Long userId;
    private String subdomain;

    public Subdomain(){}

    public Subdomain(Long userId, String subdomain){
        this.userId = userId;
        this.subdomain = subdomain;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }
}
