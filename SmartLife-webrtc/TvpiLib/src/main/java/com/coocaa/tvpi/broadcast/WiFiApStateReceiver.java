package com.coocaa.tvpi.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WiFiApStateReceiver extends BroadcastReceiver {
    private static final String TAG = WiFiApStateReceiver.class.getSimpleName();
    //热点是否开启
    public static boolean isWifiApOpen = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())) {
            int state = intent.getIntExtra("wifi_state", 0);
            Log.w(TAG, "wifi_ap_state:" + state + "(便携式热点的状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启)");
            isWifiApOpen = state == 13;
        }
    }
}
