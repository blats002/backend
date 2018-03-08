package com.divroll.hiphop.customcode.example.methods;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.divroll.hiphop.customcode.example.Config;
import com.divroll.hiphop.sdkapi.customcode.CustomCodeMethod;
import com.divroll.hiphop.sdkapi.rest.CustomCodeRequest;
import com.divroll.hiphop.sdkapi.rest.CustomCodeResponse;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DuplicateQuotaMeterMethod implements CustomCodeMethod, BaseMethod {
	@Override
	public String getMethodName() {
		return "duplicate";
	}

	@Override
	public List<String> getParams() {
		return Arrays.asList("test"); 
	}

	@Override
	public CustomCodeResponse execute(CustomCodeRequest request) {
		Map map = new LinkedHashMap();
//		String requestBody = request.getStringBody();
//		JSONObject jsonObject = JSONObject.parseObject(requestBody);

		try {
			//processQuota();
			//processMeter();
			//deleteMeterFromUserNotExist();
			//deleteQuotaFromUserNotExist();
			deleteAuthorFromUserNotExist();
		} catch (UnirestException e) {
			e.printStackTrace();
		}

		map.put("hello", "world");
		CustomCodeResponse result = new CustomCodeResponse(200, map);
		return result;
	}


	private void deleteMeterFromUserNotExist() throws UnirestException {
		HttpResponse<String> post = null;
		try {
			post = Unirest.get(Config.PARSE_URL + "/classes/Meter")
					.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
					.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
					.header("Content-Type", "application/json")
					.asString();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		String body = post.getBody();
		JSONObject results = JSONObject.parseObject(body);
		JSONArray jsonArray = results.getJSONArray("results");
		for(int i=0;i<jsonArray.size();i++) {
			JSONObject jso = jsonArray.getJSONObject(i);
			String userId = jso.getJSONObject("user").getString("objectId");
			String meterId = jso.getString("objectId");
			HttpResponse<String> get = Unirest.get(Config.PARSE_URL + "/classes/_User")
					.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
					.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
					.header("Content-Type", "application/json")
					.asString();
			String userGetBody = get.getBody();
			JSONObject result = JSONObject.parseObject(userGetBody);
			JSONArray resultJSONArray = result.getJSONArray("results");
			boolean hasUser = false;
			for(int j=0;j<resultJSONArray.size();j++) {
				JSONObject userObject = resultJSONArray.getJSONObject(j);
				String objectId = userObject.getString("objectId");
				if(userId.equals(objectId)) {
					hasUser = true;
				}
			}
			if(!hasUser) {
				System.out.println("Has user = " + hasUser);
				deleteMeter(meterId);
				// Delete meter
			}
		}

	}

	private void deleteQuotaFromUserNotExist() throws UnirestException {
		HttpResponse<String> post = null;
		try {
			post = Unirest.get(Config.PARSE_URL + "/classes/Quota")
					.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
					.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
					.header("Content-Type", "application/json")
					.asString();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		String body = post.getBody();
		JSONObject results = JSONObject.parseObject(body);
		JSONArray jsonArray = results.getJSONArray("results");
		for(int i=0;i<jsonArray.size();i++) {
			JSONObject jso = jsonArray.getJSONObject(i);
			String userId = jso.getJSONObject("user").getString("objectId");
			String quotaId = jso.getString("objectId");
			HttpResponse<String> get = Unirest.get(Config.PARSE_URL + "/classes/_User")
					.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
					.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
					.header("Content-Type", "application/json")
					.asString();
			String userGetBody = get.getBody();
			JSONObject result = JSONObject.parseObject(userGetBody);
			JSONArray resultJSONArray = result.getJSONArray("results");
			boolean hasUser = false;
			for(int j=0;j<resultJSONArray.size();j++) {
				JSONObject userObject = resultJSONArray.getJSONObject(j);
				String objectId = userObject.getString("objectId");
				if(userId.equals(objectId)) {
					hasUser = true;
				}
			}
			if(!hasUser) {
				System.out.println("Has user = " + hasUser);
				deleteQuota(quotaId);
				// Delete meter
			}
		}

	}

	private void deleteAuthorFromUserNotExist() throws UnirestException {
		HttpResponse<String> post = null;
		try {
			post = Unirest.get(Config.PARSE_URL + "/classes/Author")
					.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
					.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
					.header("Content-Type", "application/json")
					.asString();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		String body = post.getBody();
		JSONObject results = JSONObject.parseObject(body);
		JSONArray jsonArray = results.getJSONArray("results");
		for(int i=0;i<jsonArray.size();i++) {
			JSONObject jso = jsonArray.getJSONObject(i);
			String userId = jso.getJSONObject("user").getString("objectId");
			String authorId = jso.getString("objectId");
			HttpResponse<String> get = Unirest.get(Config.PARSE_URL + "/classes/_User")
					.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
					.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
					.header("Content-Type", "application/json")
					.asString();
			String userGetBody = get.getBody();
			JSONObject result = JSONObject.parseObject(userGetBody);
			JSONArray resultJSONArray = result.getJSONArray("results");
			boolean hasUser = false;
			for(int j=0;j<resultJSONArray.size();j++) {
				JSONObject userObject = resultJSONArray.getJSONObject(j);
				String objectId = userObject.getString("objectId");
				if(userId.equals(objectId)) {
					hasUser = true;
				}
			}
			if(!hasUser) {
				System.out.println("Has user = " + hasUser);
				// Delete author
				deleteAuthor(authorId);
			}
		}

	}

	private void processMeter() throws UnirestException {
		HttpResponse<String> post = null;
		try {
			post = Unirest.get(Config.PARSE_URL + "/classes/Meter")
					.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
					.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
					.header("Content-Type", "application/json")
					.asString();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		String body = post.getBody();
		//System.out.println(body);
		JSONObject results = JSONObject.parseObject(body);
		JSONArray jsonArray = results.getJSONArray("results");
		Multimap<String,String> multimap = ArrayListMultimap.create();
		List<String> userIds = new LinkedList<String>();
		for(int i=0;i<jsonArray.size();i++) {
			JSONObject jso = jsonArray.getJSONObject(i);
			String userId = jso.getJSONObject("user").getString("objectId");
			String meterId = jso.getString("objectId");
//			System.out.println("=======================================================================================");
//			System.out.println("User  ID: " + userId);
//			System.out.println("Meter ID: " + meterId);
//			System.out.println("=======================================================================================");
			multimap.put(userId, meterId);
			userIds.add(userId);
			//System.out.println(jso.toJSONString());
		}

		// FIRST PASS
		for(String userId : userIds) {
			Collection<String> values = multimap.get(userId);
			if(values.size() > 1) {
				System.out.println("=======================================================================================");
				System.out.println("DUPLICATED: " + userId);
				JSONObject keep = null;
				// First pass
				for(String s : values) {
					//System.out.println(s);
					String json = getMeter(s);
					JSONObject jsonObject = JSONObject.parseObject(json);
					// assign the first item as keep initially
					if(keep == null) {
						System.out.println("Keeping " + jsonObject.getString("objectId"));
						keep = jsonObject;
					}
					Long storage = jsonObject.getLong("storage") != null ? jsonObject.getLong("storage") : 0;
					Long compute = jsonObject.getLong("compute") != null ? jsonObject.getLong("compute") : 0;
					Long traffic = jsonObject.getLong("traffic") != null ? jsonObject.getLong("traffic") : 0;
					if((storage > 0) || (compute > 0) || (traffic > 0)) {
						System.out.println("Found new that has value keep this instead " + jsonObject.getString("objectId"));
						keep = jsonObject;
					}
				}
				// Second pass
				for(String s : values) {
					if(keep != null) {
						String objectId = keep.getString("objectId");
						if(s.equals(objectId)) {
							System.out.println("Keeping " + s);
						} else {
							System.out.println("Deleting " + s);
							deleteMeter(s);
						}
					}
				}
				System.out.println("=======================================================================================");
			}
		}
	}

	private void processQuota() throws UnirestException {
		HttpResponse<String> post = null;
		try {
			post = Unirest.get(Config.PARSE_URL + "/classes/Quota")
					.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
					.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
					.header("Content-Type", "application/json")
					.asString();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		String body = post.getBody();
		//System.out.println(body);
		JSONObject results = JSONObject.parseObject(body);
		JSONArray jsonArray = results.getJSONArray("results");
		Multimap<String,String> multimap = ArrayListMultimap.create();
		List<String> userIds = new LinkedList<String>();
		for(int i=0;i<jsonArray.size();i++) {
			JSONObject jso = jsonArray.getJSONObject(i);
			String userId = jso.getJSONObject("user").getString("objectId");
			String meterId = jso.getString("objectId");
//			System.out.println("=======================================================================================");
//			System.out.println("User  ID: " + userId);
//			System.out.println("Meter ID: " + meterId);
//			System.out.println("=======================================================================================");
			multimap.put(userId, meterId);
			userIds.add(userId);
			//System.out.println(jso.toJSONString());
		}

		for(String userId : userIds) {
			Collection<String> values = multimap.get(userId);
			if(values.size() > 1) {
				System.out.println("=======================================================================================");
				System.out.println("DUPLICATED: " + userId);
				String keep = null; // keep only one
				for(String s : values) {
					if(keep == null) {
						keep = s;
					}
					if(!s.equals(keep)) {
						deleteQuota(s);
					}
					//System.out.println(s);
					//getQuota(s);
				}
				System.out.println("=======================================================================================");
			}
		}
	}



	private String getMeter(String objectId) throws UnirestException {
		HttpResponse<String> get = Unirest.get(Config.PARSE_URL + "/classes/Meter/" + objectId)
				.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
				.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
				.header("Content-Type", "application/json")
				.asString();
		String body = get.getBody();
		//System.out.println(body);
		return body;
	}

	private void deleteMeter(String objectId) throws UnirestException {
		HttpResponse<String> get = Unirest.delete(Config.PARSE_URL + "/classes/Meter/" + objectId)
				.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
				.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
				.header("Content-Type", "application/json")
				.asString();
		String body = get.getBody();
		System.out.println(body);
	}

	private void getQuota(String objectId) throws UnirestException {
		HttpResponse<String> get = Unirest.get(Config.PARSE_URL + "/classes/Quota/" + objectId)
				.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
				.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
				.header("Content-Type", "application/json")
				.asString();
		String body = get.getBody();
		System.out.println(body);
	}

	private void deleteQuota(String objectId) throws UnirestException {
		HttpResponse<String> get = Unirest.delete(Config.PARSE_URL + "/classes/Quota/" + objectId)
				.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
				.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
				.header("Content-Type", "application/json")
				.asString();
		String body = get.getBody();
		System.out.println(body);
	}

	private void deleteAuthor(String objectId) throws UnirestException {
		HttpResponse<String> get = Unirest.delete(Config.PARSE_URL + "/classes/Author/" + objectId)
				.header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
				.header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
				.header("Content-Type", "application/json")
				.asString();
		String body = get.getBody();
		System.out.println(body);
	}

}
