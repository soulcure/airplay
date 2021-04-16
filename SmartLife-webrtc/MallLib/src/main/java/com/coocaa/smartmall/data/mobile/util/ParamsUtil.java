package com.coocaa.smartmall.data.mobile.util;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ParamsUtil {

    public static RequestBody map2RequestBody(Map<String, Object> params) {
        try {
            JSONObject json = new JSONObject();
            for (String key : params.keySet()) {
                System.out.println("key= " + key + " and value= " + params.get(key));
                json.put(key, params.get(key));
            }
            return RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
