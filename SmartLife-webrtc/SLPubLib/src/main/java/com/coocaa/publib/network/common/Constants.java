package com.coocaa.publib.network.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

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
    private static String TVPI_DOMAIN;

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
    public static final String SIGN_COOCAA_SECRET = "X123oGzsAPuRfjo8";

    /**
     * 酷开登录CLIENT_ID
     */
    //新电视派，谢光财给我们分配的 at 2018-04-28
//    public static final String COOCAA_CLIENT_ID = "7050748df941410d8ed46172fb72eefe";
    // 测试环境的用
    public static final String COOCAA_CLIENT_ID =  "56e0a352e69644d787adf859b7ec73af";
    /*！！！！！！！！！！！！！！正式 测试 配对使用！！！！！！！！！！！！！！！！*/

    public static String getPublibTvpiDomain(){
        if (TextUtils.isEmpty(TVPI_DOMAIN)) {
            TVPI_DOMAIN = (String) getMetaData(SmartScreenKit.getContext(), SmartScreenKit.getContext().getPackageName(), "TVPI_DOMAIN");
        }

        if (TextUtils.isEmpty(TVPI_DOMAIN)) {
            //默认用正式环境
            TVPI_DOMAIN = "https://tvpi.coocaa.com";
        }
        return TVPI_DOMAIN;
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
