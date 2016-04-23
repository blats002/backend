package com.divroll.core.rest.resource.gae;

import com.divroll.core.rest.Metric;
import com.divroll.core.rest.Subdomain;
import com.divroll.core.rest.guice.SelfInjectingServerResource;
import com.divroll.core.rest.resource.MetricsResource;
import com.divroll.core.rest.service.KinveyService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kinvey.java.Query;
import com.kinvey.nativejava.AppData;
import com.kinvey.nativejava.Client;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by Kerby on 4/23/2016.
 */
public class GaeMetricsServerResource extends SelfInjectingServerResource
	implements MetricsResource {

	final static Logger LOG
			= LoggerFactory.getLogger(GaeMetricsServerResource.class);

	@Inject
	@Named("kinvey.appkey")
	private String appkey;

	@Inject
	@Named("kinvey.mastersecret")
	private String masterSecret;

	@Inject
	private KinveyService kinveyService;

	@Override public void createMetric(Representation entity) {
		try {
			Form form= new Form(entity);
			String subdomain = form.getFirstValue("subdomain");
			String numbytes = form.getFirstValue("numbytes");
			LOG.info("Subdomain: " + subdomain);
			LOG.info("Byte count: " + numbytes);
			System.out.println("Subdomain: " + subdomain);
			System.out.println("Byte count: " + numbytes);

			Client kinvey = new Client.Builder(appkey, masterSecret).build();
			kinvey.enableDebugLogging();
			Boolean ping = kinvey.ping();
			kinvey.user().loginBlocking(appkey, masterSecret).execute();

			AppData<Metric> metrics = kinvey.appData("metrics", Metric.class);
			Metric metric = new Metric();
			metric.setSubdomain(subdomain);
			metric.setNumBytes(Long.valueOf(numbytes));
			metrics.saveBlocking(metric).execute();
		}catch (Exception e){
			setStatus(Status.SERVER_ERROR_INTERNAL);
			e.printStackTrace();
		}

	}
}
