package com.apiblast.customcode.example.methods;

import com.alibaba.fastjson.JSONObject;
import com.apiblast.customcode.example.Config;
import com.apiblast.sdkapi.MethodVerb;
import com.apiblast.sdkapi.customcode.CustomCodeMethod;
import com.apiblast.sdkapi.rest.CustomCodeRequest;
import com.apiblast.sdkapi.rest.CustomCodeResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import java.util.*;

public class ApplicationsMethod implements CustomCodeMethod {

	@Override
	public String getMethodName() {
		return "applications";
	}

	@Override
	public List<String> getParams() {
		return Arrays.asList("sessionToken");
	}

	@Override
	public CustomCodeResponse execute(CustomCodeRequest request) {
		String body = request.getBody();
		Map map = new LinkedHashMap();

		JSONObject jsonBody = JSONObject.parseObject(body);
		String sessionToken = jsonBody.getString("sessionToken");

		int status = -1;
		try {
			HttpResponse<String> meRequest = Unirest.get(Config.PARSE_URL + "/users/me")
					.header("X-Parse-Application-Id", Config.PARSE_APP_ID)
					.header("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY)
					.header("X-Parse-Session-Token", sessionToken)
					.header("Content-Type", "application/json")
					.asString();
			String meBody = meRequest.getBody();
			status = meRequest.getStatus();
			JSONObject meObject = JSONObject.parseObject(meBody);
			String userId = meObject.getString("objectId");
			if(userId != null) {
				if(request.getVerb().equals(MethodVerb.GET)) {
					JSONObject pointer = new JSONObject();
					pointer.put("__type", "Pointer");
					pointer.put("className", "_User");
					pointer.put("objectId", userId);
					JSONObject where = new JSONObject();
					where.put("userId", pointer);
					HttpResponse<String> getRequest = Unirest.get(Config.PARSE_URL + "/classes/Application")
							.header("X-Parse-Application-Id", Config.PARSE_APP_ID)
							.header("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY)
							.header("X-Parse-Session-Token", sessionToken)
							.header("Content-Type", "application/json")
							.queryString("where", where.toJSONString())
							.asString();
					String getBody = getRequest.getBody();
					status = getRequest.getStatus();
					JSONObject jsonObject = JSONObject.parseObject(getBody);
					Iterator<String> it = jsonObject.keySet().iterator();
					while(it.hasNext()) {
						String key = it.next();
						Object value = jsonObject.get(key);
						map.put(key, value);
					}
				} else if(request.getVerb().equals(MethodVerb.POST)) {

				} else if(request.getVerb().equals(MethodVerb.PUT)) {
					map.put("error", "Method not allowed");
				} else if(request.getVerb().equals(MethodVerb.DELETE)) {
					map.put("error", "Method not allowed");
				}
			}
		} catch (Exception e) {
			map.put("error", e.getMessage());
			e.printStackTrace();
		}
		CustomCodeResponse result = new CustomCodeResponse(status, map);
		return result;

	}

}
