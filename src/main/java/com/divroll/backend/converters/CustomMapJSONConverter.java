/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2019, Divroll, and individual contributors
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
package com.divroll.backend.converters;

import com.divroll.backend.model.dto.LinkDTO;
import com.divroll.backend.model.dto.UserDTO;
import com.google.gson.Gson;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CustomMapJSONConverter implements Converter {
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        LinkDTO linkDTO = (LinkDTO) source;
        writer.startNode("link");;
        String json = new Gson().toJson(linkDTO);
        writer.setValue(json);
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        UserDTO userDTO = new UserDTO();
        while(reader.hasMoreChildren()) {
            reader.moveDown();
            String key = reader.getNodeName(); // nodeName aka element's name
            String value = reader.getValue();
            reader.moveUp();
        }
        return userDTO;
    }

    @Override
    public boolean canConvert(Class type) {
        if(LinkDTO.class.isAssignableFrom(type)) {
            return true;
        }
        return false;
    }
}
