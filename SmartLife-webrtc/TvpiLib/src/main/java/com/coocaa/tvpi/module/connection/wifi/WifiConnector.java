package com.coocaa.tvpi.module.connection.wifi;


import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.coocaa.tvpi.util.GpsUtil;
import com.coocaa.tvpi.util.WifiUtil;
import com.coocaa.tvpi.util.permission.PermissionsUtil;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.SUPPLICANT_STATE_CHANGED_ACTION;
import static com.coocaa.tvpi.util.WifiUtil.connectWifi;
import static com.coocaa.tvpi.util.WifiUtil.registerReceiver;
import static com.coocaa.tvpi.util.WifiUtil.unregisterReceiver;
import static com.coocaa.tvpi.module.connection.wifi.WifiConnectErrorCode.CONNECT_TIMEOUT;
import static com.coocaa.tvpi.module.connection.wifi.WifiConnectErrorCode.NO_GPS_PERMISSION;
import static com.coocaa.tvpi.module.connection.wifi.WifiConnectErrorCode.NO_LOCATION_PERMISSION;
import static com.coocaa.tvpi.module.connection.wifi.WifiConnectErrorCode.NO_OPEN_WIFI;


public class WifiConnector {
    private static final String TAG = "WifiConnect";
    private final Context context;
    private String wifiSSID;
    private String wifiPsd;
    private WifiConnectCallback wifiConnectCallBack;
    private WifiConnectReceiver wifiConnectReceiver;
    private WifiConnectTimeoutHandler timeoutHandler;

    private WifiConnector(Context context) {
        this.context = context;
    }

    public static WifiConnector withContext(@NonNull final Context context) {
        return new WifiConnector(context);
    }

    public WifiConnector connect(String wifiSSID, String wifiPsd, WifiConnectCallback callBack) {
        this.wifiSSID = wifiSSID;
        this.wifiPsd = wifiPsd;
        this.wifiConnectCallBack = callBack;
        return this;
    }

    public void start() {
        if (!WifiUtil.isWifiEnabled(context)) {
            wifiConnectCallBack.onConnectFail(NO_OPEN_WIFI);
            return;
        }

        if (!GpsUtil.isOpen(context)) {
            wifiConnectCallBack.onConnectFail(NO_GPS_PERMISSION);
            return;
        }

        if (!PermissionsUtil.getInstance().hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            wifiConnectCallBack.onConnectFail(NO_LOCATION_PERMISSION);
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        WifiConnectCallbackProxy wifiConnectCallbackProxy = new WifiConnectCallbackProxy(wifiConnectCallBack);
        wifiConnectReceiver = new WifiConnectReceiver(wifiSSID, wifiConnectCallbackProxy);
        timeoutHandler = new WifiConnectTimeoutHandler(handler, wifiConnectCallbackProxy);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NETWORK_STATE_CHANGED_ACTION);
        //api28已经无法接收该广播
        intentFilter.addAction(SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(context, wifiConnectReceiver, intentFilter);
        timeoutHandler.startTimeout(20000);
        connectWifi(context, wifiSSID, wifiPsd);
    }


    private class WifiConnectCallbackProxy implements WifiConnectCallback {
        private final WifiConnectCallback wifiConnectCallback;

        public WifiConnectCallbackProxy(WifiConnectCallback wifiConnectCallback) {
            this.wifiConnectCallback = wifiConnectCallback;
        }

        @Override
        public void onConnectSuccess() {
            timeoutHandler.stopTimeout();
            unregisterReceiver(context, wifiConnectReceiver);
            if (wifiConnectCallback != null) {
                wifiConnectCallback.onConnectSuccess();
            }
        }

        @Override
        public void onConnectFail(WifiConnectErrorCode errorCode) {
            timeoutHandler.stopTimeout();
            unregisterReceiver(context, wifiConnectReceiver);
            if (wifiConnectCallback != null) {
                wifiConnectCallback.onConnectFail(CONNECT_TIMEOUT);
            }
        }
    }
}
