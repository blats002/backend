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
package com.divroll.core.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

/**
 * Install this module to arrange for SelfInjectingServerResource instances to
 * have their members injected (idempotently) by the doInit method (which is
 * called automatically after construction).
 * 
 * Incubator code. Not available in maven
 * 
 * @see https
 *      ://github.com/restlet/restlet-framework-java/blob/master/incubator/org
 *      .restlet
 *      .ext.guice/src/org/restlet/ext/guice/SelfInjectingServerResourceModule
 *      .java
 * @author Tembrel
 */
public class SelfInjectingServerResourceModule extends AbstractModule {

	@Override
	protected final void configure() {
		requestStaticInjection(SelfInjectingServerResource.class);
	}

	@Provides
	SelfInjectingServerResource.MembersInjector membersInjector(final Injector injector) {
		return new SelfInjectingServerResource.MembersInjector() {
			public void injectMembers(Object object) {
				injector.injectMembers(object);
			}
		};
	}
}
