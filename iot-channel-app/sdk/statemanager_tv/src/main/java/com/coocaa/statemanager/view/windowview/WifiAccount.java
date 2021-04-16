package com.coocaa.statemanager.view.windowview;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class WifiAccount {


    public static class WifiAcc {
        public String ssid;
        public String password;
    }

    public static WifiAcc getCurWifiPassword(Context context) {
        WifiAcc wifiAcc = new WifiAcc();

        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo info = wifiManager.getConnectionInfo();
                if (info != null) {
                    String s = info.getSSID();
                    wifiAcc.ssid = parseStr(s);
                }

                if (!TextUtils.isEmpty(wifiAcc.ssid)) {
                    Method method = wifiManager.getClass().getMethod("getPrivilegedConfiguredNetworks");
                    List<WifiConfiguration> wifiConfigurationList = (List<WifiConfiguration>) method.invoke(wifiManager);

                    for (WifiConfiguration configuration : wifiConfigurationList) {
                        String ssid = parseStr(configuration.SSID);

                        Log.d("yao", "SSID:" + configuration.SSID + ",preSharedKey:" + configuration.preSharedKey);
                        if (wifiAcc.ssid.equals(ssid)) {
                            wifiAcc.password = parseStr(configuration.preSharedKey);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wifiAcc;
    }


    private static String parseStr(String str) {
        String res = "";
        if (str.length() > 2 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
            res = str.substring(1, str.length() - 1);
        }
        return res;
    }

    /**
     * 是否是wifi连接
     *
     * @param context 场景
     */
    public static boolean isWifi(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo nInfo = cm.getActiveNetworkInfo();
                if (nInfo != null) {
                    return nInfo.getTypeName().toUpperCase(Locale.US).equals("WIFI");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
