package com.coocaa.publib;

import android.content.Context;

import com.coocaa.publib.utils.ToastUtils;

import me.jessyan.retrofiturlmanager.RetrofitUrlManager;

import static com.coocaa.smartscreen.network.api.Api.APP_STORE_DOMAIN;
import static com.coocaa.smartscreen.network.api.Api.APP_STORE_DOMAIN_NAME;
import static com.coocaa.smartscreen.network.api.Api.COOCAA_ACCOUNT_DOMAIN;
import static com.coocaa.smartscreen.network.api.Api.COOCAA_ACCOUNT_DOMAIN_NAME;
import static com.coocaa.smartscreen.network.api.Api.DEVICE_DOMAIN;
import static com.coocaa.smartscreen.network.api.Api.DEVICE_DOMAIN_NAME;
import static com.coocaa.smartscreen.network.api.Api.SKYWORTH_IOT_DOMAIN;
import static com.coocaa.smartscreen.network.api.Api.SKYWORTH_IOT_DOMAIN_NAME;
import static com.coocaa.smartscreen.network.api.Api.VOICE_ADVICE;
import static com.coocaa.smartscreen.network.api.Api.VOICE_ADVICE_NAME;
import static com.coocaa.smartscreen.network.api.Api.WX_COOCAA_DOMAIN;
import static com.coocaa.smartscreen.network.api.Api.WX_COOCAA_DOMAIN_NAME;
import static com.coocaa.smartscreen.network.api.Api.XIAOWEI_DOMAIN;
import static com.coocaa.smartscreen.network.api.Api.XIAOWEI_DUMAIN_NAME;

/**
 * @ClassName PublibHelper
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020-03-15
 * @Version TODO (write something)
 */
public class PublibHelper {

    private static Context sApp = null;
    public final static Boolean PrintLog = BuildConfig.DEBUG;

    public static void init(Context context) {
        sApp = context;

        //toast工具
        ToastUtils.getInstance().init(context);
        //服务器domain
        RetrofitUrlManager.getInstance().putDomain(XIAOWEI_DUMAIN_NAME, XIAOWEI_DOMAIN);
        RetrofitUrlManager.getInstance().putDomain(COOCAA_ACCOUNT_DOMAIN_NAME, COOCAA_ACCOUNT_DOMAIN);
        RetrofitUrlManager.getInstance().putDomain(APP_STORE_DOMAIN_NAME, APP_STORE_DOMAIN);
        RetrofitUrlManager.getInstance().putDomain(WX_COOCAA_DOMAIN_NAME, WX_COOCAA_DOMAIN);
        RetrofitUrlManager.getInstance().putDomain(DEVICE_DOMAIN_NAME, DEVICE_DOMAIN);
        RetrofitUrlManager.getInstance().putDomain(VOICE_ADVICE_NAME, VOICE_ADVICE);
        RetrofitUrlManager.getInstance().putDomain(SKYWORTH_IOT_DOMAIN_NAME, SKYWORTH_IOT_DOMAIN);
    }

    public static Context getContext() {
        return sApp;
    }

}
