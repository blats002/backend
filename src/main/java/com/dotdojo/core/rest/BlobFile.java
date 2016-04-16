/*
*
* Copyright (c) 2016 Kerby Martino and Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   https://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/
package com.divroll.core.rest;

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
