package com.coocaa.tvpi.module.connection.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.coocaa.tvpi.util.WifiUtil;


public class WifiConnectReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiConnect";
    private final String wifiSSID;
    private final WifiConnectCallback wifiConnectionCallback;
    private boolean isFirstReceiver = true;


    public WifiConnectReceiver(String wifiSSID, final WifiConnectCallback callback) {
        this.wifiSSID = wifiSSID;
        this.wifiConnectionCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent);
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            if (isFirstReceiver) {
                //过滤掉添加注册时的第一次回调
                isFirstReceiver = false;
                return;
            }
            onNetworkStateChanged(context, intent);
        } else if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
            onSupplicantStateChanged(intent);
        }
    }


    private void onNetworkStateChanged(Context context, Intent intent) {
        Log.d(TAG, "doNetworkStateChanged: " + intent);
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info != null && info.getState() != null) {
            Log.d(TAG, "doNetworkStateChanged: state:" + info.getState());
            if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                String connectedSSID = WifiUtil.getConnectWifiSsid(context);
                Log.d(TAG, "doNetworkStateChanged: connectedSSID:" + connectedSSID);
                if (connectedSSID != null && connectedSSID.equals(wifiSSID)) {
                    if (wifiConnectionCallback != null) {
                        wifiConnectionCallback.onConnectSuccess();
                    }
                }
            }
        }
    }

    private void onSupplicantStateChanged(Intent intent) {
        //api28已经无法接收该广播
        int errorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
        SupplicantState newState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
        Log.w(TAG, "errorCode:" + errorCode);
        Log.w(TAG, "newState:" + newState);
        if(errorCode == WifiManager.ERROR_AUTHENTICATING){
            Log.e(TAG, "onSupplicantStateChanged: 密码错误" );
        }
    }


}
