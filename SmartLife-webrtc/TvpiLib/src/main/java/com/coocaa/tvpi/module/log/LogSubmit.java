package com.coocaa.tvpi.module.log;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: yuzhan
 */
public class LogSubmit {

    private static BaseLogSubmit ccLogSubmit; //酷开自己的日志后台
    private static BaseLogSubmit umengLogSubmit; //友盟日志

    public static void init(Context context) {
        if (umengLogSubmit == null) {
            umengLogSubmit = new UmengLogSubmit(context);
        }
        if (ccLogSubmit == null) {
            ccLogSubmit = new CcLogSubmit(context);
        }
    }

    public static void event(String eventId, Map<String, String> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("phone_brand", Build.BRAND);
        params.put("phone_model", Build.MODEL);
        params.put("phone_android_version", Build.VERSION.RELEASE);

        if (umengLogSubmit != null) {
            umengLogSubmit.event(eventId, params);
        }
    }

    public static void allEvent(String eventId, Map<String, String> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("phone_brand", Build.BRAND);
        params.put("phone_model", Build.MODEL);
        params.put("phone_android_version", Build.VERSION.RELEASE);

        if (umengLogSubmit != null) {
            umengLogSubmit.event(eventId, params);
        }
        if (ccLogSubmit != null) {
            ccLogSubmit.event(eventId, params);
        }
    }

//    public static void umengEvent(String eventId, Map<String, String> params) {
//        if(umengLogSubmit != null) {
//            umengLogSubmit.event(eventId, params);
//        }
//    }
//
//    public static void ccEvent(String eventId, Map<String, String> params) {
//        if(ccLogSubmit != null) {
//            ccLogSubmit.event(eventId, params);
//        }
//    }
}
