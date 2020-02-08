package com.apiblast.customcode.example.methods;

import com.alibaba.fastjson.JSONObject;
import com.apiblast.sdkapi.customcode.CustomCodeMethod;
import com.apiblast.sdkapi.rest.CustomCodeRequest;
import com.apiblast.sdkapi.rest.CustomCodeResponse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegisterMethod implements CustomCodeMethod {

	@Override
	public String getMethodName() {
		return "register";
	}

	@Override
	public List<String> getParams() {
		return Arrays.asList("test"); 
	}

	@Override
	public CustomCodeResponse execute(CustomCodeRequest request) {
		Map map = new LinkedHashMap();
		String requestBody = request.getBody();
		JSONObject jsonObject = JSONObject.parseObject(requestBody);
		CustomCodeResponse result = new CustomCodeResponse(200, map);
		return result;
	}

}
