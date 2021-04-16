package com.coocaa.tvpi.module.log;

import android.app.Activity;
import android.app.Service;
import android.content.Context;

import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.IUserInfo;
import com.coocaa.tvpi.util.NetworkUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import androidx.annotation.NonNull;

/**
 * @Author: yuzhan
 */
abstract class BaseLogSubmit {
    protected Context context;

    protected static ExecutorService submitThread = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(r);
            t.setName("LogSubmit");
            return t;
        }
    });

    public BaseLogSubmit(Context context) {
        if(context instanceof Service || context instanceof Activity) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
    }

//    public abstract void pageResumeEvent(String pageName, Map<String, String> params);
//
//    public abstract void pagePausedEvent(String pageName, Map<String, String> params);
//
//    public abstract void pageFailEvent(String result, int errorCode);
//
//    public abstract void pageCustomEvent(String eventId, Map<String, String> params);
//
//    public void onActivityResume(Context context) {}
//    public void onActivityPause(Context context){}
//
//    public void onCrash(Throwable t, String type) {}

    /**
     * 提交日志事件
     * @param eventId
     * @param params
     */
    public abstract void event(String eventId, Map<String, String> params);

    protected Map<String, String> fullfilParams(Map<String, String> params) {
        Map<String, String> pp = params;
        if(params == null) {
            pp = new HashMap<>();
        }
        pp.put("phone_model", SmartConstans.getPhoneInfo().model);
        pp.put("phone_brand", SmartConstans.getPhoneInfo().brand);
        pp.put("android_version", SmartConstans.getPhoneInfo().androidVersion);
//        pp.put("app_version", String.valueOf(SmartConstans.getBuildInfo().versionCode));
        pp.put("net_type", getNetType());

//        IUserInfo userInfo = SmartApi.getUserInfo();
//        pp.put("account", userInfo == null ? "not_login" : userInfo.mobile);

        if(params != null) {
            Iterator<Map.Entry<String, String>> iter = params.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                pp.put(entry.getKey(), entry.getValue());
            }
        }
        return pp;
    }

    private String getNetType() {
        int type = NetworkUtil.getNetWorkType(context);
        switch (type) {
            case NetworkUtil.NETWORK_WIFI:
                return "wifi";
            case NetworkUtil.NETWORK_2G:
                return "2G";
            case NetworkUtil.NETWORK_3G:
                return "3G";
            case NetworkUtil.NETWORK_4G:
                return "4G";
        }
        return "unknown";
    }

    protected void submit(Runnable r) {
        submitThread.execute(r);
    }
}
