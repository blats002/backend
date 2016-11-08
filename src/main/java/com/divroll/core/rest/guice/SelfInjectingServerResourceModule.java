/*
* Copyright (c) 2016 Kerby Martino and Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   https://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/
package com.divroll.core.rest.guice;


import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

/*
*
* Copyright (c) 2016 Kerby Martino and Divroll. All Rights Reserved.
* Licensed under Divroll Commercial License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   https://www.divroll.com/licenses/LICENSE-1.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed as Proprietary and Confidential to
* Divroll and must not be redistributed in any form.
*
*/

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
