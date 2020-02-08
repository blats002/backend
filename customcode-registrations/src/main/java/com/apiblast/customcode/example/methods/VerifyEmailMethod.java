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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class VerifyEmailMethod implements CustomCodeMethod, BaseMethod {
    @Override
    public String getMethodName() {
        return "verify_email";
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
            String objectId = validateToken(email, token);
            if(objectId != null) {
                setEmailVerified(email, true);
                result = new CustomCodeResponse(200, map);
            } else {
                setEmailVerified(email, false);
                map.put("reasonPhrase", "Invalid Token");
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
        where.put("type", "EMAIL_VERIFY");
        where.put("isExpired", false);
        HttpResponse<String> get = Unirest.get(Config.PARSE_URL + "/classes/Token")
                .header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
                .header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
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
                String objectId = first.getString("objectId");
                String hash = Sha256.hash(objectId.getBytes("UTF-8"));
                if(token.equals(hash)) {
                    break;
                } else {
                    first = null;
                }
            }
            if(first != null) {
                String objectId = first.getString("objectId");
                String hash = Sha256.hash(objectId.getBytes("UTF-8"));
                if(token.equals(hash)) {
                    // Token is valid
                    // Invalidate other tokens then return
                    for(int i=0;i<jsonArray.size();i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String tokenId = jsonObject.getString("objectId");
                        invalidateToken(tokenId);
                    }
                    return objectId;
                }
            } else {
                return null;
            }

        }
        return null;
    }

    private void invalidateToken(String objectId) throws UnirestException,
            UnsupportedEncodingException, NoSuchAlgorithmException {
        JSONObject putBody = new JSONObject();
        putBody.put("isExpired", true);
        HttpResponse<String> put = Unirest.put(Config.PARSE_URL + "/classes/Token/" + objectId)
                .header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
                .header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
                .header("Content-Type", "application/json")
                .body(putBody.toJSONString())
                .asString();
    }

    private void setEmailVerified(String email, boolean verified) throws UnirestException {
        JSONObject where = new JSONObject();
        where.put("email", email);
        HttpResponse<String> get = Unirest.get(Config.PARSE_URL + "/classes/_User")
                .header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
                .header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
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
                    putBody.put("emailVerified", verified);
                    HttpResponse<String> put = Unirest.put(Config.PARSE_URL + "/classes/_User/" + objectId)
                            .header(X_PARSE_APPLICATION_ID, Config.PARSE_APP_ID)
                            .header(X_MASTER_KEY, Config.PARSE_MASTER_KEY)
                            .header("Content-Type", "application/json")
                            .body(putBody.toJSONString())
                            .asString();
                    String putResponse = put.getBody();
                    System.out.println("Verify update: " + putResponse);
                }
            }

        }
    }


}
