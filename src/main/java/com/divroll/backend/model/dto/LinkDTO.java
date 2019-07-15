/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019-present, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.backend.model.dto;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class LinkDTO implements Serializable {
    private String linkName;
    @XStreamImplicit(itemFieldName = "entities")
    private List<EntityDTO> entities;

    public LinkDTO() {}

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public List<EntityDTO> getEntities() {
        if(entities == null) {
            entities = new LinkedList<>();
        }
        return entities;
    }

    public void setEntities(List<EntityDTO> entities) {
        this.entities = entities;
    }
}
