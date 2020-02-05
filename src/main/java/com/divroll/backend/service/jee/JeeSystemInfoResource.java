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
package com.divroll.backend.service.jee;

import com.divroll.core.rest.resource.BaseServerResource;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.ClientInfo;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class JeeSystemInfoResource extends BaseServerResource {
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
