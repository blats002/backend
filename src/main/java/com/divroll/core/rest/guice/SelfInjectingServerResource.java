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

import org.restlet.resource.ServerResource;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for ServerResources that do their own member injection. Not
 * available in maven.
 * 
 * @see http
 *      ://tembrel.blogspot.co.uk/2012/03/restlet-guice-extension-considered.
 *      html
 * @see https 
 *      ://github.com/restlet/restlet-framework-java/blob/master/incubator/org
 *      .restlet
 *      .ext.guice/src/org/restlet/ext/guice/SelfInjectingServerResource.java
 * @author Tembrel
 * 
 */
public abstract class SelfInjectingServerResource extends ServerResource {

	/**
	 * Implemented by DI framework-specific code. For example, with Guice, the
	 * statically-injected MembersInjector just calls
	 * {@code injector.injectMembers(object)}.
	 */
	public interface MembersInjector {
		void injectMembers(Object object);
	}

	/**
	 * Subclasseses overriding this method must call {@code super.doInit()}
	 * first.
	 */
	protected void doInit() {
		ensureInjected(theMembersInjector);
	}

	@Inject
	private void injected() { // NOPMD
		injected.set(true);
	}

	void ensureInjected(MembersInjector membersInjector) {
		if (injected.compareAndSet(false, true)) {
			membersInjector.injectMembers(this);
		}
	}

	/**
	 * Whether we've been injected yet. This protects against multiple injection
	 * of a subclass that gets injected before doInit is called.
	 */
	private final AtomicBoolean injected = new AtomicBoolean(false);

	/**
	 * Must be statically injected by DI framework.
	 */
	@Inject
	private static volatile MembersInjector theMembersInjector;
}
