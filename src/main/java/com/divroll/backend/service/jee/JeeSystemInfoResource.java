/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.service.jee;

import com.divroll.backend.resource.jee.BaseServerResource;
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
