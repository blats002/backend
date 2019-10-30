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
package com.divroll.backend.resource.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.guice.SelfInjectingServerResource;
import com.divroll.backend.repository.SuperuserRepository;
import com.divroll.backend.resource.SuperuserActivateResource;
import com.divroll.backend.service.WebTokenService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.util.Map;

public class JeeSuperuserActivateServerResource extends SelfInjectingServerResource implements
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
        if(activationToken == null || activationToken.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }

        Map<String,Object> parsed = webTokenService.readToken(masterSecret, activationToken);
        if(parsed != null) {
            String expiration = (String) parsed.get(Constants.JWT_ID_EXPIRATION);
            String userId = (String) parsed.get(Constants.JWT_ID_KEY);
            // TODO: Check if token is expired
            boolean activated = superuserRepository.activateUser(userId);
            if(activated) {
                setStatus(Status.SUCCESS_ACCEPTED);
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        }
        return null;
    }
}
