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

public class ApplicationMethod implements CustomCodeMethod {

	@Override
	public String getMethodName() {
		return "application";
	}

	@Override
	public List<String> getParams() {
		return Arrays.asList("sessionToken", "objectId");
	}

	@Override
	public CustomCodeResponse execute(CustomCodeRequest request) {
		String body = request.getBody();
		Map map = new LinkedHashMap();

		JSONObject jsonBody = JSONObject.parseObject(body);
		String sessionToken = jsonBody.getString("sessionToken");
		String objectId = jsonBody.getString("objectId");

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
					HttpResponse<String> get = Unirest.get(Config.PARSE_URL + "/classes/Application/" + objectId)
							.header("X-Parse-Application-Id", Config.PARSE_APP_ID)
							.header("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY)
							.header("X-Parse-Session-Token", sessionToken)
							.header("Content-Type", "application/json")
							.asString();
					String getBody = get.getBody();
					status = get.getStatus();
					JSONObject jsonObject = JSONObject.parseObject(getBody);
					Iterator<String> it = jsonObject.keySet().iterator();
					while(it.hasNext()) {
						String key = it.next();
						Object value = jsonObject.get(key);
						map.put(key, value);
					}
				} else if(request.getVerb().equals(MethodVerb.POST)) {
					map.put("error", "Method not allowed");
				} else if(request.getVerb().equals(MethodVerb.PUT)) {
					HttpResponse<String> put = Unirest.put(Config.PARSE_URL + "/classes/Application/" + objectId)
							.header("X-Parse-Application-Id", Config.PARSE_APP_ID)
							.header("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY)
							.header("X-Parse-Session-Token", sessionToken)
							.header("Content-Type", "application/json")
							.asString();
					String putBody = put.getBody();
					status = put.getStatus();
					JSONObject jsonObject = JSONObject.parseObject(putBody);
					Iterator<String> it = jsonObject.keySet().iterator();
					while(it.hasNext()) {
						String key = it.next();
						Object value = jsonObject.get(key);
						map.put(key, value);
					}
				} else if(request.getVerb().equals(MethodVerb.DELETE)) {
					HttpResponse<String> delete = Unirest.delete(Config.PARSE_URL + "/classes/Application/" + objectId)
							.header("X-Parse-Application-Id", Config.PARSE_APP_ID)
							.header("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY)
							.header("X-Parse-Session-Token", sessionToken)
							.header("Content-Type", "application/json")
							.asString();
					String deleteBody = delete.getBody();
					if(deleteBody == null || deleteBody.isEmpty()) {
						deleteBody = new JSONObject().toJSONString();
					}
					status = delete.getStatus();
					JSONObject jsonObject = JSONObject.parseObject(deleteBody);
					Iterator<String> it = jsonObject.keySet().iterator();
					while(it.hasNext()) {
						String key = it.next();
						Object value = jsonObject.get(key);
						map.put(key, value);
					}
				}
			} else {
				map.put("error", "userId is required");
			}
		} catch (Exception e) {
			map.put("error", e.getMessage());
			e.printStackTrace();
		}
		CustomCodeResponse result = new CustomCodeResponse(status, map);
		return result;

	}

}
