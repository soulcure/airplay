package com.coocaa.tvpi.util;


import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;

import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.repository.utils.SmartScreenKit;
import com.coocaa.tvpi.event.UserLoginEvent;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;

import org.greenrobot.eventbus.EventBus;

/**
 * 退出账号
 * Created by songxing on 2020/6/17
 */
public class LogoutHelp {
    public static void logout() {
        UserInfoCenter.getInstance().clearUserInfo();
        EventBus.getDefault().post(new UserLoginEvent(false));
        SSConnectManager.getInstance().leaveRoom();
        //必须先reset再disconnect
        SSConnectManager.getInstance().resetLsid("lsid,", "token", "");
        MobileRequestService.getInstance().clearLoginToken();

        clearCookies();
    }

    private static void clearCookies() {
        CookieSyncManager.createInstance(SmartScreenKit.getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
        }
        WebStorage.getInstance().deleteAllData();
    }


    public static void LogoutAndReLogin(){
        logout();
        LoginActivity.start(SmartScreenKit.getContext(),true);
    }
}
