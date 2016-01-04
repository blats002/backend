package com.divroll.webdash.client.shared;

import com.hunchee.twist.annotations.Entity;
import com.hunchee.twist.annotations.Id;

import java.io.Serializable;

@Entity
public class Value implements Serializable {
    @Id
    private Long id;
    private String type;
    private String value;

    public Value(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
