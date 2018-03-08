package com.divroll.hiphop.customcode.example.methods;

import com.divroll.hiphop.sdkapi.customcode.CustomCodeMethod;
import com.divroll.hiphop.sdkapi.rest.CustomCodeRequest;
import com.divroll.hiphop.sdkapi.rest.CustomCodeResponse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kerby on 9/19/2016.
 */
public class HiMethod implements CustomCodeMethod {
    @Override
    public String getMethodName() {
        return "hi";
    }

    @Override
    public List<String> getParams() {
        return Arrays.asList("test");
    }

    @Override
    public CustomCodeResponse execute(CustomCodeRequest request) {
        String parseUrl = "";
        String masterKey = "";
        String body = request.getStringBody();
        Map map = new LinkedHashMap();
        map.put("hi", body);
        CustomCodeResponse result = new CustomCodeResponse(200, map);
        return result;
    }
}
