/*
*
* Copyright (c) 2017 Kerby Martino and Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/
package com.divroll.backend.model;

import com.alibaba.fastjson.JSONObject;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
@XStreamAlias("file")
public class File {

    private String url;
    private String name;
    private String etag;

    public File() {}

    public File(String url, String name) {
        setUrl(url);
        setName(name);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void toStream(OutputStream outputStream) {
        JSONObject fileObject = new JSONObject();
        fileObject.put("url", getUrl());
        fileObject.put("name", getName());
        final PrintStream printStream = new PrintStream(outputStream);
        printStream.print(fileObject.toJSONString());
        printStream.close();
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}
