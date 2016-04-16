package com.divroll.webdash.shared;

import java.io.Serializable;
import java.util.List;

//@XStreamAlias("file_list")
public class Files implements Serializable {
//    @XStreamImplicit(itemFieldName = "files")
    private List<File> list;
    private String cursor;

    public Files(){};

    public List<File> getList() {
        return list;
    }

    public void setList(List<File> list) {
        this.list = list;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}
