package com.coocaa.smartmall.data.tv.data;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SmartMallRequestConfig {
    public static SmartMallRequestConfig mInstance;
    private Map<String, String> mAllUrlHeaders;
    public Map<String, String> mAllDefaultHeaders;

    private static final String HEAD_CONTENT_TYPE_KEY = "Content-Type";
    private static final String HEAD_CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";

    private static final String HEAD_API_KEY = "ZENHQBFK419NIIX92VS2J8B6HLYVLAH";
    private static final String HEAD_API_TEST_KEY = "WkVOSFFCRks0MTlOSUlYOTJWUzJKOEI2SExZVkxBSFQ6";

    private static final String HEAD_AUTHORIZATION_KEY = "Authorization";
    private static String HEAD_AUTHORIZATION_VALUE = "";

    private static final String HEAD_IO_FORMAT_KEY = "Io-Format";
    private static final String HEAD_IO_FORMAT_VALUE = "JSON";
    static final String BASE_URL_DEBUG = "http://beta-voice.tvos.skysrt.com/shop_rest_service/shop/";
    static final String BASE_URL_RELEASE = "https://iot.coocaa.com/shop_rest_service/shop/";
    public static String TAB_PRODUCT_BASE_URL = BASE_URL_DEBUG;// "https://iot.coocaa.com";


    public SmartMallRequestConfig() {
        mAllUrlHeaders = new HashMap<String, String>();
        mAllDefaultHeaders = new HashMap<String, String>();

        HEAD_AUTHORIZATION_VALUE = "Basic " + getBase64(HEAD_API_KEY + ":");

        mAllDefaultHeaders.put(HEAD_API_KEY, HEAD_API_TEST_KEY);
//        mAllDefaultHeaders.put(HEAD_AUTHORIZATION_KEY , HEAD_AUTHORIZATION_VALUE);
        mAllDefaultHeaders.put(HEAD_AUTHORIZATION_KEY, "Basic " + HEAD_API_TEST_KEY);
        mAllDefaultHeaders.put(HEAD_IO_FORMAT_KEY, HEAD_IO_FORMAT_VALUE);

    }

    public static synchronized SmartMallRequestConfig getInstance() {
        if (mInstance == null) {
            mInstance = new SmartMallRequestConfig();
        }
        return mInstance;
    }

    // 编码
    public static String getBase64(String str) {
        String result = "";
        if (str != null) {
            try {
                result = new String(Base64.encode(str.getBytes("utf-8"), Base64.NO_WRAP), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 解码
    public static String getFromBase64(String str) {
        String result = "";
        if (str != null) {
            try {
                result = new String(Base64.decode(str, Base64.NO_WRAP), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public synchronized Map<String, String> putParameterToHeader(String parameterKey, String parameterValue, boolean isNewHeader) {
        dealWithHeader(isNewHeader);
        mAllUrlHeaders.put(parameterKey, parameterValue);
        return mAllUrlHeaders;
    }

    public synchronized Map<String, String> putParameterToHeader(Map<String, String> originalHead, boolean isNewHeader) {
        dealWithHeader(isNewHeader);
        mAllUrlHeaders.putAll(originalHead);
        return mAllUrlHeaders;
    }

    private void dealWithHeader(boolean isNewHeader) {
        if (mAllUrlHeaders == null) {
            mAllUrlHeaders = new HashMap<String, String>();
        } else {
            if (isNewHeader) {
                mAllUrlHeaders = null;
                mAllUrlHeaders = new HashMap<String, String>();
            }
        }
    }
//根据apk类型切换服务器地址
    public static final void initPaths(Context context) {
        try {
            boolean isRelease = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.getBoolean("isRelease");
            if(isRelease){
                TAB_PRODUCT_BASE_URL=BASE_URL_RELEASE;
                Log.i("TAG", isRelease ? "isRelease" : "debug");
                Log.i("TAG", "当前为正式服务器地址");
            }else{
                Log.i("TAG", "当前为测试服务器地址");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void setDebugMode(boolean isDebug) {
        if(!isDebug){
            TAB_PRODUCT_BASE_URL=BASE_URL_RELEASE;
            Log.i("TAG", "当前为正式服务器地址");
        }else{
            Log.i("TAG", "当前为测试服务器地址");
        }
    }

}
