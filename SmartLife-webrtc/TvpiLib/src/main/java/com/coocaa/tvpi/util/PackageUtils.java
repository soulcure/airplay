package com.coocaa.tvpi.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class PackageUtils {
    private static final String TAG = PackageUtils.class.getSimpleName();

    public static boolean isInstalledApp(Context context, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> info = packageManager.getInstalledPackages(0);
        if (info == null || info.isEmpty())
            return false;

        for (PackageInfo packageInfo : info) {
            if (pkgName.equals(packageInfo.packageName)) {
                Log.d(TAG, "isInstalledApp: true");
                return true;
            }
        }
        return false;
    }


    public static int getVersionCode(Context context, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return -1;
        }
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> info = packageManager.getInstalledPackages(0);
        if (info == null || info.isEmpty())
            return -1;
        for (PackageInfo packageInfo : info) {
            if (pkgName.equals(packageInfo.packageName)) {
                Log.d(TAG, "getVersionCode: " + packageInfo.versionCode);
                return packageInfo.versionCode;
            }
        }
        return -1;
    }


    public static String getAppName(Context context, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return "";
        }
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> info = packageManager.getInstalledPackages(0);
        if (info == null || info.isEmpty())
            return "";
        for (PackageInfo packageInfo : info) {
            if (pkgName.equals(packageInfo.packageName)) {
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                CharSequence appLabel = applicationInfo.loadLabel(packageManager);
                Log.d(TAG, "getAppName: " + appLabel);
                return appLabel.toString();
            }
        }
        return "";
    }
}
