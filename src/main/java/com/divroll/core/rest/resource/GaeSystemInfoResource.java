/*
*
* Copyright (c) 2017 Kerby Martino and Divroll. All Rights Reserved.
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
package com.divroll.core.rest.resource;

import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.ClientInfo;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.security.Role;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class GaeSystemInfoResource extends ServerResource {
    @Get("json")
    public Representation represent(){
        if(!hasAdminRole()){
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
        }
        Map<String,String> map = null;
        Context context = getContext();
        if(context != null){
            InputStream is =  context.getClass().getResourceAsStream("/git.properties");
            Properties props = new Properties();
            try {
                props.load(is);
                map = new LinkedHashMap<String, String>((Map) props);
                setStatus(Status.SUCCESS_OK);
            } catch (IOException e) {
                e.printStackTrace();
                setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        }
        return new JsonRepresentation(new JSONObject(map).toString());
    }

    protected boolean hasAdminRole() {
        ClientInfo clientInfo = getClientInfo();
        List<Role> roles = clientInfo.getRoles();
        boolean isAdmin = false;
        for (Role role : roles) {
            if (role.getName().equals("admin")) {
                isAdmin = true;
                break;
            }
        }
        return isAdmin;
    }
}
