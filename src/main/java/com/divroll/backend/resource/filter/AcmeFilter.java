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
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * GPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */
package com.divroll.backend.resource.filter;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.routing.Filter;

public class AcmeFilter extends Filter {

    public AcmeFilter(Context context) {
        super(context);
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        return super.beforeHandle(request, response);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        System.out.println("Filtered= " + request.getResourceRef().getPath());
        if(request.getResourceRef().getPath().contains("/.well-known/acme-challenge/")) {
            response.getEntity().setMediaType(MediaType.TEXT_PLAIN);
            response.getEntity().setCharacterSet(null);
            Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
            disposition.setFilename("challenge.txt");
            response.getEntity().setDisposition(disposition);
        }
        super.afterHandle(request, response);
    }

}
