package com.coocaa.smartscreen.network.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.repository.utils.SmartScreenKit;

/**
 * @ClassName Constants
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-11-29
 * @Version TODO (write something)
 */
public class Constants {

    public static final String TAG = Constants.class.getSimpleName();

    // appkey  appsalt
    public static final String APP_SALT_TVPAI = "b831444b09b74b60a5ed158e807b3dcf";
    public static final String APP_KEY_TVPAI = "aa08aeb683e180627fbb57784b4c2d9d";


    public static final String VIDEO_CALL_CLIENT_ID = "56e0a352e69644d787adf859b7ec73af";

    public static final String VIDEO_CALL_SIGN_SECRET = "X123oGzsAPuRfjo8";

    /*！！！！！！！！！！！！！！正式 测试 配对使用！！！！！！！！！！！！！！！！*/
    /**
     * 接口签名串尾部加入固定串 ------ 酷开登录
     */
//    public static final String SIGN_COOCAA_SECRET = "o1ERnx2KfSHQmpzy";
    // 测试环境的用
//    public static final String SIGN_COOCAA_SECRET = "X123oGzsAPuRfjo8";

    /**
     * 酷开登录CLIENT_ID
     */
    //新电视派，谢光财给我们分配的 at 2018-04-28
//    public static final String COOCAA_CLIENT_ID = "7050748df941410d8ed46172fb72eefe";
    // 测试环境的用
//    public static final String COOCAA_CLIENT_ID =  "56e0a352e69644d787adf859b7ec73af";
    /*！！！！！！！！！！！！！！正式 测试 配对使用！！！！！！！！！！！！！！！！*/

    // 内部网页打开参数
    public static class Cordova {
        /**
         * 访问的url
         */
        public static final String url = "url";
        /**
         * 访问的url资料id
         */
        public static final String urlResId = "urlResId";
        /**
         * 标题显示
         */
        public static final String title = "title";
        /**
         * 标题显示的资源id
         */
        public static final String titleResId = "titleResId";
    }

    private static String TVPI_DOMAIN;
    private static String COOCAA_ACCOUNT_DOMAIN;
    private static String COOCAA_SECRET;
    private static String COOCAA_CLIENT_ID;
    private static String IOT_SERVER_LOG_URL;

    public static String getTvpiDomain(){
        if (TextUtils.isEmpty(TVPI_DOMAIN)) {
            TVPI_DOMAIN = (String) getMetaData(SmartScreenKit.getContext(), SmartScreenKit.getContext().getPackageName(), "TVPI_DOMAIN");
        }

        if (TextUtils.isEmpty(TVPI_DOMAIN)) {
            //默认用正式环境
            TVPI_DOMAIN = "https://tvpi.coocaa.com";
        }
        return TVPI_DOMAIN;
    }

    public static String getCoocaaAccountDomain(){
        if (TextUtils.isEmpty(COOCAA_ACCOUNT_DOMAIN)) {
            COOCAA_ACCOUNT_DOMAIN = (String) getMetaData(SmartScreenKit.getContext(), SmartScreenKit.getContext().getPackageName(), "COOCAA_ACCOUNT_DOMAIN");
        }

        if (TextUtils.isEmpty(COOCAA_ACCOUNT_DOMAIN)) {
            //默认用正式环境
            COOCAA_ACCOUNT_DOMAIN = "https://passport.coocaa.com";
        }
        return COOCAA_ACCOUNT_DOMAIN;
    }

    public static String getCoocaaSecret(){
        if (TextUtils.isEmpty(COOCAA_SECRET)) {
            COOCAA_SECRET = (String) getMetaData(SmartScreenKit.getContext(), SmartScreenKit.getContext().getPackageName(), "COOCAA_SECRET");
        }

        if (TextUtils.isEmpty(COOCAA_SECRET)) {
            //默认用正式环境
            COOCAA_SECRET = "o1ERnx2KfSHQmpzy";
        }
        return COOCAA_SECRET;
    }

    public static String getCoocaaClientId(){
        if (TextUtils.isEmpty(COOCAA_CLIENT_ID)) {
            COOCAA_CLIENT_ID = (String) getMetaData(SmartScreenKit.getContext(), SmartScreenKit.getContext().getPackageName(), "COOCAA_CLIENT_ID");
        }

        if (TextUtils.isEmpty(COOCAA_CLIENT_ID)) {
            //默认用正式环境
            COOCAA_CLIENT_ID = "7050748df941410d8ed46172fb72eefe";
        }
        return COOCAA_CLIENT_ID;
    }

    public static String getIOTLOGServer() {
       if (TextUtils.isEmpty(IOT_SERVER_LOG_URL))
            IOT_SERVER_LOG_URL = (String) getMetaData(SmartScreenKit.getContext(), SmartScreenKit.getContext().getPackageName(), "IOT_SERVER_LOG_URL");

        if (TextUtils.isEmpty(IOT_SERVER_LOG_URL)) {
            IOT_SERVER_LOG_URL = "https://api.skyworthiot.com/";
        }
        return IOT_SERVER_LOG_URL;
//        return "https://api-sit.skyworthiot.com";
    }

    private static Object getMetaData(Context context, String packageName, String key) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA);
            if (applicationInfo != null) {
                Object value = null;
                if (applicationInfo.metaData != null) {
                    value = applicationInfo.metaData.get(key);
                }
                if (value == null) {
                    return null;
                }
                return value;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
