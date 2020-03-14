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
package com.divroll.backend.resource.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.repository.SuperuserRepository;
import com.divroll.backend.resource.SuperuserActivateResource;
import com.divroll.backend.service.WebTokenService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;

import java.util.Map;

public class JeeSuperuserActivateServerResource extends BaseServerResource implements
        SuperuserActivateResource {

    @Inject
    SuperuserRepository superuserRepository;

    @Inject
    WebTokenService webTokenService;

    @Inject
    @Named("masterSecret")
    String masterSecret;

    @Override
    public Representation activate() {
        String activationToken = getQueryValue("activationToken");
        //String email = getQueryValue("email");
        if(activationToken == null || activationToken.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        Map<String,Object> parsed = webTokenService.readToken(masterSecret, activationToken);
        if(parsed != null) {
            String expiration = (String) parsed.get(Constants.JWT_ID_EXPIRATION);
            String email = (String) parsed.get(Constants.JWT_ID_EMAIL);
            // TODO: Check if token is expired
            String userId = (String) parsed.get(Constants.JWT_ID_KEY);
            Superuser superuser = superuserRepository.getUserByEmail(email);
            superuser.setPassword(null);
            if(superuser == null) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return null;
            }
            boolean activated = superuserRepository.activateUser(email);
            if(activated) {
                String authToken = webTokenService.createToken(masterSecret, superuser.getEntityId());
                superuser.setAuthToken(authToken);
                setStatus(Status.SUCCESS_ACCEPTED);
                return new JsonRepresentation(asJSONObject(superuser));
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        return null;
    }
}
