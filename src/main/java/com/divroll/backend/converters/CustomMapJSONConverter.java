package com.divroll.backend.converters;

import com.divroll.backend.model.LinkDTO;
import com.divroll.backend.model.UserDTO;
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
