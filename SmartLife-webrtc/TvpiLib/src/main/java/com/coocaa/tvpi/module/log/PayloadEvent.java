package com.coocaa.tvpi.module.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.http.home.CcLogData;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.tvpi.module.io.HomeIOThread;

import java.util.ArrayList;
import java.util.Map;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.session.Session;

/**
 * @Author: yuzhan
 */
public class PayloadEvent {

    public static void submit(String tag, String eventName, Object eventData) {
        CcLogData data = new CcLogData();
        data.header = new CcLogData.XHeader();
        data.header.tag = tag;
        data.header.timestamp = System.currentTimeMillis();
        data.header.client = new CcLogData.XClient();
        data.header.client.appVersion = String.valueOf(SmartConstans.getBuildInfo().versionCode);
        data.header.client.appVersionName = SmartConstans.getBuildInfo().versionName;
        data.header.client.sysVersion = "Android" + SmartConstans.getPhoneInfo().androidVersion;

        CoocaaUserInfo coocaaUserInfo = Repository.get(LoginRepository.class).queryCoocaaUserInfo();
        data.header.client.udid = coocaaUserInfo == null ? "not-login" : coocaaUserInfo.mobile;
        data.header.client.brand = SmartConstans.getPhoneInfo().brand;

        data.payload = new CcLogData.XPlayload();
        data.payload.events = new ArrayList<>();
        data.payload.events.add(new CcLogData.XEvents());
        data.payload.events.get(0).data = appendData(eventData);
//        Log.d("XEvent", "eventData=" + eventData);
//        Log.d("XEvent", "appendData=" + data.payload.events.get(0).data);
        data.payload.events.get(0).eventTime = System.currentTimeMillis();
        data.payload.events.get(0).eventName = eventName;

        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                HomeHttpMethod.getInstance().submitLog(data);
            }
        });
    }

    private static Object appendData(Object eventData) {
        try {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(eventData);
            jsonObject.put("deviceType", "mobile-android");
            String lsid = getMobileLsid();
            jsonObject.put("sourceLsid", lsid == null ? "" : lsid);
            String targetLsid = getTargetLsid();
            jsonObject.put("targetLsid", targetLsid == null ? "" : targetLsid);

            String ssid = getSSID();
            jsonObject.put("wifiSSID", ssid == null ? "" : ssid);

            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(eventData instanceof Map) {
//            Map<String, Object> actualMap = new HashMap<String, Object>();
//            actualMap.putAll((Map) eventData);
//
//            actualMap.put("deviceType", "mobile");
//
//            String lsid = getMobileLsid();
//            actualMap.put("sourceLsid", lsid == null ? "" : lsid);
//
//            String targetLsid = getTargetLsid();
//            actualMap.put("targetLsid", targetLsid == null ? "" : targetLsid);
//
//            return actualMap;
//        } else {
//
//        }

        return eventData;
    }

    private static String getTargetLsid() {
        Device device = SSConnectManager.getInstance().getDevice();
        if(device != null)
            return device.getLsid();
        return "";
    }

    private static String getSSID() {
        Session connectSession = SSConnectManager.getInstance().getTarget();
        if (connectSession != null) {
            Map<String, String> extras = connectSession.getExtras();
            for (Map.Entry<String, String> entry : extras.entrySet()) {
                if ("ssid".equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return "";
    }

    private static String getMobileLsid() {
        try {
            return Repository.get(LoginRepository.class).queryDeviceRegisterLoginInfo().zpLsid;
        } catch (Exception e) {

        }
        return "";
    }
}
