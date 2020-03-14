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
package com.divroll.backend.resource.jee;

import com.divroll.backend.Constants;
import com.divroll.backend.model.Application;
import com.divroll.backend.model.Domain;
import com.divroll.backend.model.Superuser;
import com.divroll.backend.resource.DomainResource;
import jetbrains.exodus.entitystore.EntityId;
import org.mindrot.jbcrypt.BCrypt;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

public class JeeDomainServerResource extends BaseServerResource
    implements DomainResource {

    String appName;

    String domainName;

    @Override
    protected void doInit() {
        super.doInit();
        appName = getAttribute(Constants.APP_NAME);
        domainName = getAttribute(Constants.DOMAIN_NAME);
        if(domainName == null) {
            domainName = getQueryValue(Constants.DOMAIN_NAME);
        }
    }

    @Override
    public Domain retrieveDomain() {
        Superuser superuser = superuserRepository.getUserByAuthToken(superAuthToken);

        Boolean isMaster = false;
        if (theMasterToken != null
                && masterToken != null
                && BCrypt.checkpw(masterToken, theMasterToken)) {
            isMaster = true;
        }

        if(!isMaster() && superuser == null && !isMaster) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }
        Domain domain = domainService.retrieveDomain(domainName);
        if(domain != null) {
            if(superuser != null && domain.getUser().getEntityId().equals(superuser.getEntityId())) {
                setStatus(Status.SUCCESS_ACCEPTED);
                return domain;
            } else if(isMaster) {
                setStatus(Status.SUCCESS_ACCEPTED);
                return domain;
            }
        } else {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
        return null;
    }

    @Override
    public Domain attachDomain(Domain entity) {

        if(domainName == null) {
            domainName = entity.getDomainName();
        }

        if(appName == null) {
            appName = entity.getAppName();
        }

        Superuser superuser = superuserRepository.getUserByAuthToken(superAuthToken);
        if(!isMaster() && superuser == null) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }

        Application application = applicationService.readByName(appName);
        Superuser owner = application.getSuperuser();

        if(superuser.getEntityId().equals(owner.getEntityId())) {
            EntityId domainId = applicationService.attachDomain(appName, domainName, superuser);
            if(domainId != null) {
                return new Domain(domainId.toString(), domainName, appName, superuser);
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Domain already exists");
                return null;
            }
        } else {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }

    }

    @Override
    public Domain detachDomain(Domain entity) {

        if(domainName == null) {
            domainName = entity.getDomainName();
        }

        if(appName == null) {
            appName = entity.getAppName();
        }

        Superuser superuser = superuserRepository.getUserByAuthToken(superAuthToken);
        if(!isMaster() && superuser == null) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }

        Application application = applicationService.readByName(appName);
        Superuser owner = application.getSuperuser();

        if(superuser.getEntityId().equals(owner.getEntityId())) {
            boolean detached = applicationService.detachDomain(appName, domainName, superuser);
            if(detached) {
                setStatus(Status.SUCCESS_ACCEPTED);
                return null;
            } else {
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unable to detach domain");
                return null;
            }
        } else {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return null;
        }

    }
}
