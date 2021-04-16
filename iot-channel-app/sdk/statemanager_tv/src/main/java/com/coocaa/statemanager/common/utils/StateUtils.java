package com.coocaa.statemanager.common.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

public class StateUtils {

    public static String getServiceMetaData(PackageManager packageManager, String pkg) {
        String key = "ss-clientID";
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(pkg, PackageManager.GET_SERVICES);
//            Log.i(TAG, "getServiceMetaData: serviceInfo size = " + packageInfo.services.length);
            for (ServiceInfo service : packageInfo.services) {
                ComponentName cn = new ComponentName(pkg, service.name);
                ServiceInfo info = packageManager.getServiceInfo(cn, PackageManager.GET_META_DATA);
                if (info.metaData == null) {
//                    Log.i(TAG, "getServiceMetaData: service name = " + service.name + " metaData is null");
                    continue;
                }
                String data = info.metaData.getString(key);
//                Log.i(TAG, "getServiceMetaData: service name =" + service.name + "--clientid = " + data);
                if (data != null) {
                    return data;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ComponentName getTopComponet(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            return am.getRunningTasks(1).get(0).topActivity;
        } catch (Exception e) {
        }
        return null;
    }

}
