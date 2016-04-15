/**
 *
 * Copyright (c) 2016 Divroll and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.divroll.webdash.client.shared;

import com.hunchee.twist.annotations.Embedded;
import com.hunchee.twist.annotations.Entity;
import com.hunchee.twist.annotations.Id;
import org.jboss.errai.databinding.client.api.Bindable;

import java.io.Serializable;
import java.util.Date;

@Bindable
@Entity
public class File implements Serializable {
    @Id
    private Long id;
    private String fileName;
    private String fileUrl;
    private Date created;
    private Date modified;

    public File(){}

    public File(String fileName){
        this.fileName = fileName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }
}
