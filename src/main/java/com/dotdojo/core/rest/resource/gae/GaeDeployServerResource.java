package com.divroll.core.rest.resource.gae;

import com.divroll.core.rest.guice.SelfInjectingServerResource;
import com.divroll.core.rest.resource.DeployResource;
import com.divroll.core.rest.service.KinveyService;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GaeDeployServerResource extends SelfInjectingServerResource
	implements DeployResource {

	final static Logger LOG
			= LoggerFactory.getLogger(GaeDeployServerResource.class);

	private String subdomain;

	private String revision;

	private String id;

	@Inject
	@Named("kinvey.appkey")
	private String appkey;

	@Inject
	@Named("kinvey.mastersecret")
	private String masterSecret;

	@Inject
	private KinveyService kinveyService;

	@Override
	public Map createDeployment(Representation entity) throws Exception {
		Map result = new HashMap();
		subdomain = getQueryValue("subdomain");
		id = getQueryValue("id");
		revision = id;
		if(id != null || subdomain != null) {
			Queue queue = QueueFactory.getDefaultQueue();
			queue.add(TaskOptions.Builder
					.withUrl("/rest/deployments/zips")
					.param("appkey", appkey)
					.param("masterSecret", masterSecret)
					.param("subdomain", subdomain)
					.param("revision", revision));
			result.put("status", "Queued for deployment");
			setStatus(Status.SUCCESS_OK);
		} else {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "id cannot be null or empty");
		}
		return result;
	}


}
