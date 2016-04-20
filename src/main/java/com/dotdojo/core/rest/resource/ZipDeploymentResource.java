package com.divroll.core.rest.resource;

		import org.restlet.representation.Representation;
		import org.restlet.resource.Get;
		import org.restlet.resource.Post;

/**
 * Created by Kerby on 4/19/2016.
 */
public interface ZipDeploymentResource {
	@Post
	public void createDeployment(Representation entity);
}
