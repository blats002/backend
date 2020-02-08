package com.apiblast.customcode.example.methods;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.apiblast.sdkapi.customcode.CustomCodeMethod;
import com.apiblast.sdkapi.rest.CustomCodeRequest;
import com.apiblast.sdkapi.rest.CustomCodeResponse;

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
		String body = request.getBody();
		Map map = new LinkedHashMap();
		map.put("hello", body);
		CustomCodeResponse result = new CustomCodeResponse(200, map);
		return result;
	}

}
