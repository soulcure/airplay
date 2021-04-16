package com.tianci.user.api.utils;

import android.content.Context;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

    public static byte[] getByteFromBool(boolean result) {
        return String.valueOf(result).getBytes();
        //        Boolean b = result;
        //        return b.toString().getBytes();
    }

    public static boolean getBoolFromByte(byte[] data) {
        String sResult = getStringFromBytes(data);
        return parseBoolean(sResult);
    }

    public static String getStringFromBytes(byte[] body) {
        if (isEmptyBody(body)) {
            ULog.e("getStringFromBytes(), bytes is null or empty");
            return null;
        }
        return new String(body);
    }

    public static boolean isEmptyBody(byte[] body) {
        return body == null || body.length == 0;
    }

    public static boolean parseBoolean(String status) {
        return "true".equalsIgnoreCase(status);
    }

    public static String getJsonString(String json, String key) {
        String value = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            value = jsonObject.getString(key);
            ULog.i("getJsonString(), key = " + key + ", value = " + value);
        } catch (JSONException e) {
            ULog.e("getJsonString(), exception = " + e.getMessage());
            e.printStackTrace();
        }
        return value;
    }

    public static boolean getResult(Bundle bundle) {
        return bundle != null && bundle.getBoolean("result");
    }
}
