package com.divroll.core.rest;

import com.carlosbecker.guice.GuiceModules;
import com.carlosbecker.guice.GuiceTestRunner;
import com.divroll.core.rest.guice.GuiceConfigModule;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceTestRunner.class)
@GuiceModules(GuiceConfigModule.class)
public class TestDivrollApplication {
	@Test
	@Ignore
	public void testSystemInfoResource() throws IOException {
		String url = "http://localhost:8080/rest/system";
		Client client = new Client(Protocol.HTTP);
		Request request = new Request(Method.GET, url);
		Response response = client.handle(request);

		assertEquals(403, response.getStatus().getCode());
	}
}
