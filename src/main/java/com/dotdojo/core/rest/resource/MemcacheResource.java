package com.divroll.core.rest.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;

/**
 * Created by Kerby on 4/20/2016.
 */
public interface MemcacheResource {
	@Get
	public void getCache(Representation entity) throws Exception;
}
