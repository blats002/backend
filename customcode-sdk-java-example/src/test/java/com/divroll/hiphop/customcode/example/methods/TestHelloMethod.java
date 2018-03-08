package com.divroll.hiphop.customcode.example.methods;

import com.divroll.hiphop.sdkapi.MethodVerb;
import com.divroll.hiphop.sdkapi.rest.CustomCodeRequest;
import com.divroll.hiphop.sdkapi.rest.CustomCodeResponse;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class TestHelloMethod extends TestCase {

	private static final int API_VERSION = 1;
	private static final String APP_ID = "test";
	private static final String APP_KEY = "test";

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public TestHelloMethod( String testName ) {
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite( TestHelloMethod.class );
	}

	public void testHelloMethod() {
		HelloMethod method = new HelloMethod();
		Map<String, String> params = new LinkedHashMap<String, String>();
		CustomCodeRequest proccessedRequest = new CustomCodeRequest
				(MethodVerb.GET, "/test", params, "world".getBytes(), "hello_method", 0L);
		assertEquals("hello_method", method.getMethodName());
		CustomCodeResponse response = method.execute(proccessedRequest);
		assertEquals(200, response.getResponseStatus());
		assertEquals("world", response.getResponseMap().get("hello"));
	}
}
