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

import com.divroll.backend.model.Function;
import com.divroll.backend.repository.FunctionRepository;
import com.divroll.backend.resource.ManageFunctionResource;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.restlet.data.Status;

import javax.inject.Inject;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class JeeManageFunctionServerResource extends BaseServerResource
    implements ManageFunctionResource {

    private static final Logger LOG
            = LoggerFactory.getLogger(JeeManageFunctionServerResource.class);

    @Inject
    FunctionRepository functionRepository;

    String functionName;

    @Override
    protected void doInit() {
        super.doInit();
        functionName = getAttribute("functionName");
    }

    @Override
    public Function createFunction(Function entity) {
        try {
            if (!isAuthorized()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if(!isMaster()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return null;
            }
            if(entity == null) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return null;
            }
            functionRepository.createFunction(appId, namespace, functionName, entity.getJar());
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return null;
    }

    @Override
    public void removeFunction(Function entity) {
        try {
            if (!isAuthorized()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            if(!isMaster()) {
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return;
            }
            if(functionName == null || functionName.isEmpty()) {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            functionRepository.deleteFunction(appId, namespace, functionName);
        } catch (Exception e) {
            e.printStackTrace();
            setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }
}
