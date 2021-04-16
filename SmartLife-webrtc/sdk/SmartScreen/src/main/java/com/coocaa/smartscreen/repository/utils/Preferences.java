package com.coocaa.smartscreen.repository.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * SharePreference
 * Created by songxing on 2020/7/24
 */
public class Preferences {
    private static final String SP_NAME = "smartLifeSp";

    public Preferences() {
        throw new RuntimeException("can not new it");
    }


    public static class Device {
        //连接设备列表
        private static final String KEY_CONNECT_DEVICE_LIST = "connectDeviceList";

        public static String getConnectDeviceList() {
            return getString(KEY_CONNECT_DEVICE_LIST);
        }

        public static void saveConnectDeviceList(String connectDeviceList) {
            saveString(KEY_CONNECT_DEVICE_LIST, connectDeviceList);
        }
    }

    public static class Login {
        //token
        private static final String KEY_ACCESS_TOKEN = "accessToken";
        //tp_token
        private static final String KEY_TP_TOKEN_INFO = "tpToken";
        //设备注册信息
        private static final String KEY_DEVICE_REGISTER_LOGIN = "deviceRegisterLogin";
        //云信用户信息
        private static final String KEY_YUNXIN_USER_INFO = "yunXinUserInfo";
        //酷开用户信息
        private static final String KEY_COOCAA_USER_INFO = "coocaaUserInfo";


        public static String getAccessToken() {
            return getString(KEY_ACCESS_TOKEN);
        }

        public static String getDeviceRegisterLoginInfo() {
            return getString(KEY_DEVICE_REGISTER_LOGIN);
        }

        public static String getCoocaaUserInfoJson() {
            return getString(KEY_COOCAA_USER_INFO);
        }

        public static String getYunXinUserInfoJson() {
            return getString(KEY_YUNXIN_USER_INFO);
        }

        public static String getTpTokenInfoJson() {
            return getString(KEY_TP_TOKEN_INFO);
        }

        public static void saveAccessToken(String accessToken) {
            saveString(KEY_ACCESS_TOKEN, accessToken);
        }

        public static void saveDeviceRegisterLoginInfo(String deviceRegisterLogin) {
            saveString(KEY_DEVICE_REGISTER_LOGIN, deviceRegisterLogin);
        }

        public static void saveYunXinUserInfo(String yunXinUserInfoJson) {
            saveString(KEY_YUNXIN_USER_INFO, yunXinUserInfoJson);
        }

        public static void saveCoocaaUserInfo(String coocaaUserInfoJson) {
            saveString(KEY_COOCAA_USER_INFO, coocaaUserInfoJson);
        }

        public static void saveTpToken(String tpTokenInfoJson) {
            saveString(KEY_TP_TOKEN_INFO, tpTokenInfoJson);
        }
    }

    public static class VideoCall {
        //视频通话联系人列表
        private static final String KEY_CONTACTS_LIST = "contactsList";


        public static String getContactsList() {
            return getString(KEY_CONTACTS_LIST);
        }

        public static void saveContactsList(String contactsListJson) {
            saveString(KEY_CONTACTS_LIST, contactsListJson);
        }
    }

    public static class VoiceAdvice {

        private static final String KEY_VOICE_ADVICE = "voiceAdvice";

        private static final String KEY_UPDATE = "updateTime";

        public static String getVoiceAdvice() {
            return getString(KEY_VOICE_ADVICE);
        }

        public static void saveVoiceAdvice(String voiceAdvice) {
            saveString(KEY_VOICE_ADVICE, voiceAdvice);
        }

        public static String getUpdateTime() {
            return getString(KEY_UPDATE);
        }

        public static void saveUpdateTime(String updateTime) {
            saveString(KEY_UPDATE, updateTime);
        }

    }

    public static class LiveTipConfirm {

        private static final String KEY_TIP_CONFIRM = "tipConfirm";

        public static boolean getLiveTipConfirm() {
            if("true".equals(getString(KEY_TIP_CONFIRM))){
                return true;
            } else if("false".equals(getString(KEY_TIP_CONFIRM))) {
                return false;
            }
            return false;
        }

        public static void saveLiveTipConfirm(boolean isTipConfirm) {
            if (isTipConfirm) {
                saveString(KEY_TIP_CONFIRM, "true");
            } else{
                saveString(KEY_TIP_CONFIRM, "false");
            }
        }

    }

    public static class App {
        //本地搜索记录
        private static final String KEY_SEARCH_LIST = "searchList";
        //电视应用列表
        private static final String KEY_SEARCH_TV_APP_LIST = "tvAppList";

        public static String getSearchList() {
            return getString(KEY_SEARCH_LIST);
        }

        public static void saveSearchList(String searchListJson) {
            saveString(KEY_SEARCH_LIST, searchListJson);
        }

        public static String getTvAppList() {
            return getString(KEY_SEARCH_TV_APP_LIST);
        }

        public static void saveTvAppList(String tvAppListJson) {
            saveString(KEY_SEARCH_TV_APP_LIST, tvAppListJson);
        }
    }

    public static class PushContinueTip {

        private static final String KEY_TIP_CONTINUE = "tipContinue";

        public static boolean getPushContinueTip() {
            if ("true".equals(getString(KEY_TIP_CONTINUE))) {
                return true;
            } else if ("false".equals(getString(KEY_TIP_CONTINUE))) {
                return false;
            }
            return false;
        }

        public static void savePushContinueTip(boolean isPushContinue) {
            if (isPushContinue) {
                saveString(KEY_TIP_CONTINUE, "true");
            } else {
                saveString(KEY_TIP_CONTINUE, "false");
            }
        }
    }

    public static class Mall {
        private static final String KEY_ADDRESS_LIST = "addressList";

        public static String getAddressList() {
            return getString(KEY_ADDRESS_LIST);
        }

        public static void saveAddressList(String addressListJson) {
            saveString(KEY_ADDRESS_LIST, addressListJson);
        }
    }

    private static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getString(String key) {
        return getSharedPreferences().getString(key, null);
    }

    public static void clear() {
        SharedPreferences preferences = SmartScreenKit.getContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    static SharedPreferences getSharedPreferences() {
        Context context = SmartScreenKit.getContext();
        if (context == null) {
            throw new IllegalArgumentException("need set context first");
        }
        return context.getSharedPreferences(SP_NAME, context.MODE_PRIVATE);
    }
}
