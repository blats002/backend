package com.divroll.core.rest.resource;

import org.restlet.representation.Representation;
import org.restlet.resource.Post;

/**
 * Created by Kerby on 4/23/2016.
 */
public interface MetricsResource {
	@Post
	public void createMetric(Representation entity);
}
