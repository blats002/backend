package com.divroll.webdash.shared;

import java.io.Serializable;
import java.util.List;

public class Subdomains implements Serializable {
    private List<Subdomain> list;
    private String cursor;

    public Subdomains(){};

    public List<Subdomain> getList() {
        return list;
    }

    public void setList(List<Subdomain> list) {
        this.list = list;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
