package com.coocaa.smartscreen.constant;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.coocaa.smartscreen.utils.SuperSpUtil;

/**
 * @Author: yuzhan
 */
public final class SmartConstans {

    private static BuildInfo buildInfo;
    private static PhoneInfo phoneInfo = PhoneInfo.PhoneInfoBuilder.builder().build();
    private static Context context;
    private static BusinessInfo businessInfo;

    public static void setAppContext(Context appContext) {
        if(appContext instanceof Application) {
            context = appContext;
        } else {
            context = appContext.getApplicationContext();
        }
    }

    public static void setBuildInfo(BuildInfo _buildInfo) {
        if(buildInfo == null && _buildInfo != null) {
            buildInfo = _buildInfo;
        }
    }

    public static BuildInfo getBuildInfo() {
        if(buildInfo == null) {
            return BuildInfo.BuildInfoBuilder.builder().build();
        }
        return buildInfo;
    }

    public static PhoneInfo getPhoneInfo() {
        return phoneInfo;
    }

    public static void setBusinessInfo(BusinessInfo _businessInfo) {
        if(businessInfo == null && _businessInfo != null)
            businessInfo = _businessInfo;
    }

    public static BusinessInfo getBusinessInfo() {
        if(businessInfo == null) {
            return BusinessInfo.BusinessInfoBuilder.builder().build();
        }
        return businessInfo;
    }

    public static boolean isBetaServer() {
        return BETA_CHANNEL.equals(getBuildInfo().buildChannel);
    }

    public static boolean isTestServer() {
        if(TEST_CHANNEL.equals(getBuildInfo().buildChannel)) {
            return true;
        }
        if(context != null) {
            String flag = SuperSpUtil.getString(context, "smartscreen_server_flag");
            Log.d("SmartConfig", "smartscreen_server_flag=" + flag);
            return "test".equals(flag);
        }
        return isTestBuildChannel();
    }

    public static boolean isTestBuildChannel() {
        return TEST_CHANNEL.equals(getBuildInfo().buildChannel) || BETA_CHANNEL.equals(getBuildInfo().buildChannel);
    }

    private final static String TEST_CHANNEL = "Atest1";
    private final static String BETA_CHANNEL = "Abeta1";
}
