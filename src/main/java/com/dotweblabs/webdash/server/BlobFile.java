package com.divroll.webdash.server;

import com.google.appengine.api.datastore.Blob;
import com.hunchee.twist.annotations.Entity;
import com.hunchee.twist.annotations.Id;

import java.io.Serializable;

@Entity
public class BlobFile implements Serializable {
    @Id
    private String filename;
    private Blob blob;

    public BlobFile(){}

    public BlobFile(String filename){
        this.filename = filename;
    }

    public BlobFile(String filename, Blob blob){
        this.filename = filename;
        this.blob = blob;
    }

    public Blob getBlob() {
        return blob;
    }

    public void setBlob(Blob blob) {
        this.blob = blob;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
