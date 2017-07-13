package com.apiblast.customcode.example.methods;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.apiblast.customcode.example.Config;
import com.apiblast.customcode.example.helper.Sha256;
import com.apiblast.sdkapi.customcode.CustomCodeMethod;
import com.apiblast.sdkapi.rest.CustomCodeRequest;
import com.apiblast.sdkapi.rest.CustomCodeResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jdk.nashorn.internal.parser.JSONParser;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ResetPasswordMethod implements CustomCodeMethod, BaseMethod {
    @Override
    public String getMethodName() {
        return "reset_password";
    }

    @Override
    public List<String> getParams() {
        return null;
    }

    @Override
    public CustomCodeResponse execute(CustomCodeRequest customCodeRequest) {
        Map map = new LinkedHashMap();
        String requestBody = customCodeRequest.getBody();
        CustomCodeResponse result = null;
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String email = jsonObject.getString("email");
            String token = jsonObject.getString("token");
            String newPassword = jsonObject.getString("password");
            String objectId = validateToken(email, token);
            if(objectId != null) {
                setNewPassword(objectId, newPassword);
                result = new CustomCodeResponse(200, map);
            } else {
                result = new CustomCodeResponse(400, map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = new CustomCodeResponse(500, map);
        }
        return result;
    }

    private String validateToken(String email, String token) throws UnirestException,
            UnsupportedEncodingException, NoSuchAlgorithmException {
        JSONObject where = new JSONObject();
        where.put("email", email);
        where.put("type", "RESET_PASSWORD");
        where.put("isExpired", false);
        HttpResponse<String> get = Unirest.get(Config.TXTSTREET_PARSE_URL + "/classes/Token")
                .header(X_PARSE_APPLICATION_ID, Config.TXTSTREET_PARSE_APP_ID)
                .header(X_MASTER_KEY, Config.TXTSTREET_MASTER_KEY)
                .header("Content-Type", "application/json")
                .queryString("where", where.toJSONString())
                .asString();
        String body = get.getBody();
        JSONObject jsonBody = JSONObject.parseObject(body);
        JSONArray jsonArray = jsonBody.getJSONArray("results");
        if(jsonArray != null && !jsonArray.isEmpty()) {
            JSONObject first = null;
            for(int i=0;i<jsonArray.size();i++) {
                first = jsonArray.getJSONObject(i);
                break;
            }
            String objectId = first.getString("objectId");
            String hash = Sha256.hash(objectId.getBytes("UTF-8"));
            if(token.equals(hash)) {
                // Token is valid
                for(int i=0;i<jsonArray.size();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String tokenId = jsonObject.getString("objectId");
                    invalidateToken(tokenId);
                }
                return objectId;
            }
        }
        return null;
    }

    private void invalidateToken(String objectId) throws UnirestException,
            UnsupportedEncodingException, NoSuchAlgorithmException {
        JSONObject putBody = new JSONObject();
        putBody.put("isExpired", true);
        HttpResponse<String> put = Unirest.put(Config.TXTSTREET_PARSE_URL + "/classes/Token/" + objectId)
                .header(X_PARSE_APPLICATION_ID, Config.TXTSTREET_PARSE_APP_ID)
                .header(X_MASTER_KEY, Config.TXTSTREET_MASTER_KEY)
                .header("Content-Type", "application/json")
                .body(putBody.toJSONString())
                .asString();
    }

    private void setNewPassword(String email, String newPassword) throws UnirestException {
        JSONObject where = new JSONObject();
        where.put("email", email);
        HttpResponse<String> get = Unirest.get(Config.TXTSTREET_PARSE_URL + "/classes/Token/")
                .header(X_PARSE_APPLICATION_ID, Config.TXTSTREET_PARSE_APP_ID)
                .header(X_MASTER_KEY, Config.TXTSTREET_MASTER_KEY)
                .header("Content-Type", "application/json")
                .queryString("where", where.toJSONString())
                .asString();
        String body = get.getBody();
        JSONObject jsonBody = JSONObject.parseObject(body);
        JSONArray jsonArray = jsonBody.getJSONArray("results");
        if(jsonArray != null && !jsonArray.isEmpty()) {
            for(int i=0;i<jsonArray.size();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if(jsonObject != null) {
                    String objectId = jsonObject.getString("objectId");
                    JSONObject putBody = new JSONObject();
                    putBody.put("password", newPassword);
                    HttpResponse<String> put = Unirest.put(Config.TXTSTREET_PARSE_URL + "/classes/_User/" + objectId)
                            .header(X_PARSE_APPLICATION_ID, Config.TXTSTREET_PARSE_APP_ID)
                            .header(X_MASTER_KEY, Config.TXTSTREET_MASTER_KEY)
                            .header("Content-Type", "application/json")
                            .body(putBody.toJSONString())
                            .asString();
                    String putResponse = put.getBody();
                    System.out.println("Reset password update: " + putResponse);
                }
            }

        }
    }

}
