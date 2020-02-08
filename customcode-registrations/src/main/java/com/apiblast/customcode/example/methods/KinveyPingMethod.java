package com.apiblast.customcode.example.methods;

import com.apiblast.sdkapi.customcode.CustomCodeMethod;
import com.apiblast.sdkapi.rest.CustomCodeRequest;
import com.apiblast.sdkapi.rest.CustomCodeResponse;
//import com.kinvey.nativejava.Client;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class KinveyPingMethod implements CustomCodeMethod {

	private static final Logger LOG
			= Logger.getLogger(HelloMethod.class.getName());

	@Override public String getMethodName() {
		return "ping_method";
	}

	@Override public List<String> getParams() {
		return null;
	}

	@Override public CustomCodeResponse execute(CustomCodeRequest request) {
		Map map = new LinkedHashMap();
		/*try {
			String appId = request.getAppId();
			String appKey = request.getAppKey();
			Client kinvey = new Client.Builder(appId, appKey).build();
			kinvey.enableDebugLogging();
			Boolean ping = null;
			ping = kinvey.ping();
			LOG.info("Client ping -> " + ping);
			map.put("ping", ping);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		CustomCodeResponse result = new CustomCodeResponse(200, map);
		return result;
	}
}
