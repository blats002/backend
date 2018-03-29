package com.apiblast.customcode.example.methods;

import com.alibaba.fastjson.JSONObject;
import com.apiblast.customcode.example.Config;
import com.apiblast.sdkapi.customcode.CustomCodeMethod;
import com.apiblast.sdkapi.rest.CustomCodeRequest;
import com.apiblast.sdkapi.rest.CustomCodeResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import java.util.*;

public class LoginMethod implements CustomCodeMethod {

	@Override
	public String getMethodName() {
		return "login";
	}

	@Override
	public List<String> getParams() {
		return Arrays.asList("username", "password");
	}

	@Override
	public CustomCodeResponse execute(CustomCodeRequest request) {
		String body = request.getBody();
		Map map = new LinkedHashMap();

		JSONObject jsonBody = JSONObject.parseObject(body);
		String username = jsonBody.getString("username");
		String password = jsonBody.getString("password");

		int status = -1;
		try {
			HttpResponse<String> getRequest = Unirest.get(Config.PARSE_URL + "/login")
					.header("X-Parse-Application-Id", Config.PARSE_APP_ID)
					.header("X-Parse-REST-API-Key", Config.PARSE_REST_API_KEY)
					.header("Content-Type", "application/json")
					.queryString("username", username)
					.queryString("password", password)
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
		} catch (Exception e) {
			map.put("error", e.getMessage());
			e.printStackTrace();
		}
		CustomCodeResponse result = new CustomCodeResponse(status, map);
		return result;

	}

}
