package com.swaiotos.skymirror.sdk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @ClassName: DLNACommonUtil
 * @Description: java类作用描述
 * @Date: 2019/9/18 12:04
 */
public class DLNACommonUtil {

    public static InetAddress getLocaleAddress(Context context) {
        if (context == null)
            return null;
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        InetAddress ia = null;
        try {

            ia = InetAddress.getByName(String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ia;
    }

    public static boolean checkPermission(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            @SuppressLint("WrongConstant")
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            Log.d("lfzzzz", "uid --- " + ai.uid);


            if (ai.uid == 1000)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("lfzzzz", "checkPermission: " + e.getMessage());
            return false;
        }
        return false;
    }
}
