package com.divroll.hiphop.customcode.example.methods;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.divroll.hiphop.sdkapi.customcode.CustomCodeMethod;
import com.divroll.hiphop.sdkapi.rest.CustomCodeRequest;
import com.divroll.hiphop.sdkapi.rest.CustomCodeResponse;

public class HelloMethod implements CustomCodeMethod {

	@Override
	public String getMethodName() {
		return "hello_method";
	}

	@Override
	public List<String> getParams() {
		return Arrays.asList("test"); 
	}

	@Override
	public CustomCodeResponse execute(CustomCodeRequest request) {
		String body = request.getStringBody();
		Map map = new LinkedHashMap();
		map.put("hello", body);
		CustomCodeResponse result = new CustomCodeResponse(200, map);
		return result;
	}

}
