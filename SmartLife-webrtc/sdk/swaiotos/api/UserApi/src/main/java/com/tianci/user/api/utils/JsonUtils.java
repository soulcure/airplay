package com.tianci.user.api.utils;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonUtils {

//    public static String toJson(LoginTypeData data) {
//        try {
//            JSONObject object = new JSONObject();
//            object.put("accountType", data.accountType).put("extraData", data.extraData);
//            return object.toString();
//        } catch (Exception e) {
//            ULog.e("toJson(), exception = " + e.getMessage());
//        }
//        return "";
//    }
//
//    public static DBManagerImpl.BasicInfo toBasicInfo(String json) {
//        DBManagerImpl.BasicInfo info = new DBManagerImpl.BasicInfo();
//        try {
//            JSONObject object = new JSONObject(json);
//            info.did = object.getString("did");
//            info.mac = object.getString("mac");
//            info.sid = object.getString("sid");
//            info.uid = object.getString("uid");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return info;
//    }

    public static String toJson(Map<String, String> map) {
        try {
            return new JSONObject(map).toString();
        } catch (Exception e) {
            ULog.e("toJson(), exception = " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    public static Map<String, String> toMapV0(String json) {
        Map<String, String> map = new HashMap<>();
        try {
            JSONObject jsonObj = new JSONObject(json);
            Iterator<String> keysItr = jsonObj.keys();
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                Object value = jsonObj.get(key);
                if (key != null && value != null) {
                    map.put(key, String.valueOf(value));
                }
            }
        } catch (Exception e) {
            ULog.e("toMapV0(), exception = " + e.getMessage());
            e.printStackTrace();
        }

        return map;
    }

    public static Map<String, Object> toMap(String json) {
        if (!TextUtils.isEmpty(json)) {
            try {
                return toMap(new JSONObject(json));
            } catch (Exception e) {
                ULog.i("toMap(), exception = " + e.getMessage());
                e.printStackTrace();
            }
        }

        return new HashMap<>(0);
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        if (object == JSONObject.NULL) {
            return map;
        }

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(String json) {
        try {
            return toList(new JSONArray(json));
        } catch (Exception e) {
            ULog.i("toList(), exception = " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>(0);
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
