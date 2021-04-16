package com.coocaa.smartsdk;

import android.app.Activity;


import com.coocaa.smartsdk.internal.SimpleSmartApiListenerWrapper;
import com.coocaa.smartsdk.internal.SmartApiBinder;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.smartsdk.object.IUserInfo;
import com.coocaa.smartsdk.pay.PayManager;

import java.util.Map;

/**
 * @Author: yuzhan
 */
public class SmartApi {

    public static boolean isMobileRuntime() {
        return SmartApiBinder.getInstance().isMobileRuntime();
    }

    /**
     * 获取账户信息
     * @return 账户信息，null表示未登录
     */
    public static IUserInfo getUserInfo() {
        return SmartApiBinder.getInstance().getUserInfo();
    }

    /**
     * 启动账户登录页面
     */
    public static void showLoginUser() {
        SmartApiBinder.getInstance().showLoginUser();
    }

    /**
     * 获取账户信息，并且增加注册监听回调
     * @param listener
     * @param showLoginIfLogout
     * @return
     */
    public static IUserInfo getUserInfo(UserChangeListener listener, boolean showLoginIfLogout) {
        return SmartApiBinder.getInstance().getUserInfo(listener, showLoginIfLogout);
    }

    /**
     * 移除注册回调
     * @param listener
     */
    public static void removeUserChangeListener(UserChangeListener listener) {
        SmartApiBinder.getInstance().removeUserChangeListener(listener);
    }

    public static void updateAccessToken(String token) {
        SmartApiBinder.getInstance().updateAccessToken(token);
    }

    /**
     * 检测是否已连接设备，并注册回调
     * @param listener
     * @param args
     * @return
     */
    public static boolean simpleCheckDeviceConnected(SimpleSmartApiListener listener, Object args) {
        SmartApiBinder.getInstance().simpleCheckDeviceConnected(new SimpleSmartApiListenerWrapper(listener, args));
        return SmartApiBinder.getInstance().isDeviceConnect();
    }

    /**
     * 监听注册回调
     * @param listener
     * @return
     */
    public static boolean addListener(SmartApiListener listener) {
        SmartApiBinder.getInstance().addListener(listener);
        return SmartApiBinder.getInstance().isDeviceConnect();
    }

    /**
     * 移除注册回调
     * @param listener
     */
    public static void removeListener(SmartApiListener listener) {
        SmartApiBinder.getInstance().removeListener(listener);
    }

    /**
     * 启动连接设备页面
     */
    public static void startConnectDevice() {
        SmartApiBinder.getInstance().startConnectDevice();
    }

    /**
     * 判断是否已连接设备
     * @return
     */
    public static boolean isDeviceConnect() {
        return SmartApiBinder.getInstance().isDeviceConnect();
    }

    /**
     * 判断是否有设备连接，无论是否可以推送
     * @return
     */
    public static boolean hasDevice() {
        return SmartApiBinder.getInstance().hasDevice();
    }

    /**
     * 获取连接设备信息
     * @return
     */
    public static ISmartDeviceInfo getConnectDeviceInfo() {
        return SmartApiBinder.getInstance().getConnectDeviceInfo();
    }

    /**
     * 连接本店WIFI
     */
    public static void startConnectSameWifi(String networkForceKey) {
        SmartApiBinder.getInstance().startConnectSameWifi(networkForceKey);
    }

    public static void submitLog(String name, Map<String, String> params) {
        SmartApiBinder.getInstance().submitLog(name, params);
    }

    public static void submitLogWithTag(String tag, String name, Map<String, String> params) {
        SmartApiBinder.getInstance().submitLogWithTag(tag, name, params);
    }

    public static boolean isSameWifi() {
        return SmartApiBinder.getInstance().isSameWifi();
    }



    /**
     *开始支付
     */
    public static void startPay(Activity activity, String id, String json){
        PayManager.startPay(activity, id, json);
    }

    public static void setMsgDispatchEnable(String clientId, boolean enable) {
        SmartApiBinder.getInstance().setMsgDispatchEnable(clientId, enable);
    }

    /**
     * 启动微信小程序
     */
    public static void startWxMP(String id, String path) {
        SmartApiBinder.getInstance().startWxMP(id, path);
    }

    /**
     * 调整到应用商店
     */
    public static void startAppStore(String pkg) {
        SmartApiBinder.getInstance().startAppStore(pkg);
    }

    public static void requestBindCode(String requestId) {
        SmartApiBinder.getInstance().requestBindCode(requestId);
    }
}
