/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
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

import com.divroll.backend.model.Application;
import com.divroll.backend.repository.EntityRepository;
import com.divroll.backend.repository.jee.AppEntityRepository;
import com.divroll.backend.resource.FunctionResource;
import com.divroll.backend.service.ApplicationService;
import com.google.inject.Inject;
import org.mozilla.javascript.*;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeFunctionServerResource extends BaseServerResource
    implements FunctionResource {

    private Response response = new Response();

    public class Response {
        private String body;
        public void complete(int code) {
            System.out.println("Completed " + code + " with body - " + body);
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

    @Inject
    ApplicationService applicationService;

    @Inject
    EntityRepository entityRepository;

    @Override
    public Representation getMethod(Representation entity) {
//        AppEntityRepository appEntityRespository = new AppEntityRepository(entityRepository, appId, "User");
//        Object evaluated = eval(appEntityRespository, response,"var id = '1-2'; response.body = 'Hello world'; response.complete(200);");
//        return new StringRepresentation(evaluated.toString());
        return null;
    }

    @Override
    public Representation postMethod(Representation entity) {
        return null;
    }

    @Override
    public Representation putMethod(Representation entity) {
        return null;
    }

    @Override
    public Representation deleteMethod(Representation entity) {
        return null;
    }

}
