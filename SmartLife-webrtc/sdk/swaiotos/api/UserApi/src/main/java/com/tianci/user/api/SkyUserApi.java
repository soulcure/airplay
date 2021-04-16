package com.tianci.user.api;

import android.content.Context;

import com.tianci.user.api.factory.UserImplFactory;
import com.tianci.user.api.listener.OnAccountChangedListener;
import com.tianci.user.api.utils.ContextUtils;
import com.tianci.user.api.utils.ULog;

import java.util.Map;

public class SkyUserApi {
    public enum AccountType {
        coocaa,
        qq,
        weixin,
    }

    public static final String ACTION_ACCOUNT_EXPIRED = "com.tianci.user.account_expired";
    public static final String TAG = "SkyUserApi";


    public static String getAppId() {
        return "101180898"; // "101161688";101180898
    }

    /**
     * 是否有积分、金币功能，默认关。2016年1月7日<br/>
     *
     * @return boolean
     */
    public static boolean supportBonusPoint() {
        return false;
    }

    /**
     * @param context void 显示账号管理主界面<br/>
     * @date 2015年4月17日
     */
    public static void showAccountManager(Context context) {
        showAccountManager(context, false, null);
    }

    /**
     * 显示账号管理主界面。2015年7月11日<br/>
     *
     * @param context
     * @param needFinish 登录完成后是否需要退出账户界面。
     */
    public static void showAccountManager(Context context, boolean needFinish) {
        showAccountManager(context, needFinish, null);
    }

    public static void showAccountManager(Context context, boolean needFinish,
                                          Map<String, String> extraData) {
        if (context == null) {
            ULog.e(TAG, "showAccountManager, context is null , return...");
            return;
        }

        LaunchAccount.launch(context, needFinish, extraData);
    }

    private IUser userImpl;

    public SkyUserApi(Context context) {
        ContextUtils.init(context);
        userImpl = UserImplFactory.create();
        ULog.i(TAG, "user impl = " + userImpl);
        // userApi = new ContentProviderUserApiImpl(getAppContext(context));
    }

    public boolean registerAccountChanged(OnAccountChangedListener listener) {
        return userImpl.registerAccountChanged(listener);
    }

    public boolean unregisterAccountChanged(OnAccountChangedListener listener) {
        return userImpl.unregisterAccountChanged(listener);
    }

    /**
     * 判断当前电视是否有账号登录<br/>
     * 2015年4月16日
     *
     * @return boolean
     */
    public boolean hasLogin() {
        boolean result = userImpl.hasLogin();
        ULog.i(TAG, "hasLogin, result = " + result);
        return result;
    }

    /**
     * 获取账号相关Token。2015年4月16日<br/>
     *
     * @param type token类型
     * @return String
     */
    public String getToken(String type) {
        ULog.i(TAG, "getToken, type = " + type);
        return userImpl.getToken(type);
    }

    /**
     * 获取当前登录的账号信息<br/>
     * 具体数据参考：https://beta.passport.coocaa.com/html2/user-guide.html# 中的“获取用户信息”
     */
    public Map<String, Object> getAccoutInfo() {
        return userImpl.getInfo();
    }

    public void logout() {
        ULog.i(TAG, "logout start");
        userImpl.logout();
    }

    public boolean loginByToken(String accessToken) {
        return userImpl.loginByToken(accessToken);
    }

    /**
     * 概述：获取当前用户session，2013-12-11<br/>
     *
     * @return String
     */
    public String getSession() {
        return userImpl.getSession();
    }

    /**
     * 判断当前设备账号是否是公网地址<br/>
     * 账户是否登陆状态不影响该方法，异常情况下，默认是true
     *
     * @return boolean true 表示当前设备账户是公网地址 false 表示当前非公网设备。
     */
    public boolean isPublicAddress() {
        return userImpl.isPublicAddress();
    }

    /**
     * 触发账户的刷新信息的动作
     */
    public void refreshAccountInfo() {
        boolean result = userImpl.refreshAccountInfo();
        ULog.i(TAG, "refreshAccountInfo result = " + result);
    }

}
