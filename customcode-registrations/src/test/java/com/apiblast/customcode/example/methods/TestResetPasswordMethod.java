package com.apiblast.customcode.example.methods;

import com.alibaba.fastjson.JSONObject;
import com.apiblast.sdkapi.MethodVerb;
import com.apiblast.sdkapi.rest.CustomCodeRequest;
import com.apiblast.sdkapi.rest.CustomCodeResponse;
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
public class TestResetPasswordMethod extends TestCase {

	private static final int API_VERSION = 1;
	private static final String APP_ID = "";
	private static final String APP_KEY = "";

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public TestResetPasswordMethod(String testName ) {
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite( TestResetPasswordMethod.class );
	}

	public void testResetPasswordMethod() {
		ResetPasswordMethod method = new ResetPasswordMethod();
		Map<String, String> params = new LinkedHashMap<String, String>();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("token", "d4d0b7f0-8f70-4673-9e07-aff6e53a7ece");
		jsonObject.put("email", "***REMOVED***");
		jsonObject.put("password", "new_password");
		String body = jsonObject.toJSONString();


		CustomCodeRequest proccessedRequest = new CustomCodeRequest
				(MethodVerb.GET, "", params, body, APP_ID, APP_KEY, "reset_password", 0L);
		assertEquals("reset_password", method.getMethodName());
		CustomCodeResponse response = method.execute(proccessedRequest);

		assertEquals(200, response.getResponseStatus());

	}
}
