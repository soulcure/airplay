package com.coocaa.publib.connect.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.google.gson.Gson;

import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.PhoneDeviceInfo;
import swaiotos.channel.iot.ss.manager.lsid.LSIDManager;
import swaiotos.channel.iot.utils.EmptyUtils;

public class MySSService extends SSChannelService {

    private static final SSChannelServiceManager manager = new SSChannelServiceManager() {

        private LSIDManager mLSIDManager;

        @Override
        public void performCreate(Context context) {
            Log.d("MySSService", "performCreate: ");
            //可以做登录注册操作，但是手机端不必要写
            mLSIDManager = new LSIDInfoManager(getContext());
        }

        @Override
        public DeviceInfo getDeviceInfo(Context context) {
            String open_id = "";
            String nick_name = "";
            try {
                CoocaaUserInfo coocaaUserInfo = Repository.get(LoginRepository.class).queryCoocaaUserInfo();
                Log.d("SSChannelServiceManager", "getDeviceInfo: " + new Gson().toJson(coocaaUserInfo));
                open_id = coocaaUserInfo.open_id;
                nick_name = coocaaUserInfo.nick_name;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new PhoneDeviceInfo("12345",
                    open_id, nick_name, Build.MODEL, "", "");
        }

        @Override
        public boolean performClientVerify(Context context, ComponentName cn, String id, String key) {
            return true;
        }

        @Override
        public LSIDManager getLSIDManager() {
            return mLSIDManager;
        }

        @Override
        public void onSSChannelServiceStarted(Context context) {

        }

        @Override
        public Intent getClientServiceIntent(Context context) {
            Intent intent = new Intent("swaiotos.intent.action.channel.iot.SSCLIENT");
            return intent;
        }
    };

    @Override
    protected SSChannelServiceManager getManager() {
        return manager;
    }
//    }
//
//    private String sid;
//
//    @Override
//    protected String performCreate() {
//        SharedPreferences mPreference = getSharedPreferences("screenid", Context.MODE_PRIVATE);
//        sid = mPreference.getString("sid", "");
//        if (EmptyUtils.isEmpty(sid)) {
//            sid = "SID" + String.valueOf((int) (Math.random() * 10000000));
//            mPreference.edit().putString("sid", sid).commit();
//        }
//        return sid;
//    }
//
//    @Override
//    protected boolean performVerify(String packageName, String id, String key) {
//
////        String sign = ApkUtils.getSingInfo(getApplicationContext(), packageName, ApkUtils.SHA1);
////
////        String s = F.string2Md5(id + sign);
////        return s.equals(sign);
//        return true;
//    }
//
//    @Override
//    protected String clientServiceAction() {
//        return null;
//    }
//
//    @Override
//    protected void onSSServiceCreated() {
//
//    }
//
//
//    //    private static String getIMEI(Context context) {
////        String imei = "";
////        try {
////            TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
////            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
////                imei = tm.getDeviceId();
////            } else {
////                Method method = tm.getClass().getMethod("getImei");
////                imei = (String) method.invoke(tm);
////            }
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////        return imei;
////    }
}
