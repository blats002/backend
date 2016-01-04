package com.divroll.webdash.client.shared;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Kerby on 1/5/2016.
 */
public class Files implements Serializable {
    private List<Blog> list;
    private String cursor;

    public Files(){};

    public List<Blog> getList() {
        return list;
    }

    public void setList(List<Blog> list) {
        this.list = list;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
