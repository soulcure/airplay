package com.tianci.user.api;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tianci.user.api.utils.ULog;

/**
 * Created by wen on 2017/6/16.
 */

public class LaunchAccount {
    private static final String TAG = "LaunchAccount";
    private static final String COOCAA_ACCOUNT = "swaiotos.service.user.account_setting";
    private static final String USER = "com.tianci.user";
    private static final String USER_MOBILE = "com.skyworth.smartsystem.vhome";
    private static final String USER_PAD = "swaiotos.user.pad";
    private static final String USER_UI = "com.tianci.user.ui";
    private static final String SETTING = "com.tianci.setting";

    public static boolean launch(Context context, boolean needFinish, Map<String, String> extraData) {
        if (context == null) {
            ULog.e(TAG, "launch, context == null");
            return false;
        }

        final PackageManager packageManager = context.getPackageManager();

        final Intent intentNew = new Intent(COOCAA_ACCOUNT);
        //检索所有可用于新的Action的活动。如果没有匹配的活动，则返回一个空列表。
        List<ResolveInfo> listNew =
                packageManager.queryIntentActivities(intentNew, PackageManager.MATCH_DEFAULT_ONLY);
        if (listNew != null && !listNew.isEmpty()) {
            String packageName = listNew.get(0).activityInfo.packageName;
            return launchUserAccount(context, packageName, needFinish, extraData);
        } else {
            final Intent intent = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT);
            //检索所有可用于给定的意图进行的活动。如果没有匹配的活动，则返回一个空列表。
            List<ResolveInfo> list =
                    packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            if (list != null && list.size() > 0) {
                ULog.i(TAG, "ResolveInfo size = " + list.size());
                String packageName = getResponsePackageName(list);
                ULog.i(TAG, "packageName = " + packageName);
                return launchUserAccount(context, packageName, needFinish, extraData);
            } else {
                ULog.e(TAG, "show account, no app response ACTION_ADD_ACCOUNT");
                return false;
            }
        }
    }

    private static boolean launchUserAccount(Context context, String packageName,
                                             boolean needFinish, Map<String, String> extraData) {
        ULog.i(TAG, "launch account pkgName = " + packageName);

        Intent intent = new Intent();
        addExtraData(intent, extraData);
        intent.setAction(android.provider.Settings.ACTION_ADD_ACCOUNT);
        intent.putExtra("pkgName", context.getPackageName());
        intent.putExtra("needFinish", needFinish);
        intent.setPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ULog.i(TAG, "launch account start");
            context.startActivity(intent);
            ULog.i(TAG, "launch account success");
            return true;
        } catch (Exception e) {
            ULog.e(TAG, "startActivity exception = " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static void addExtraData(Intent intent, Map<String, String> extraData) {
        ULog.i(TAG, "extraData = " + extraData);
        if (extraData != null && !extraData.isEmpty()) {
            Set<Map.Entry<String, String>> entrySet = extraData.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private static String getResponsePackageName(List<ResolveInfo> list) {
        if (list.size() == 1) {
            return list.get(0).activityInfo.packageName;
        } else {
            HashSet<String> actionSet = new HashSet<String>();
            for (ResolveInfo resolveInfo : list) {
                actionSet.add(resolveInfo.activityInfo.packageName);
            }
            ULog.i(TAG, "action set = " + actionSet);

            if (actionSet.contains(USER)) {
                return USER;
            } else if (actionSet.contains(USER_MOBILE)) {
                return USER_MOBILE;
            } else if (actionSet.contains(USER_PAD)) {
                return USER_PAD;
            } else if (actionSet.contains(USER_UI)) {
                return USER_UI;
            } else if (actionSet.contains(SETTING)) {
                return SETTING;
            } else {
                return actionSet.iterator().next();
            }
        }
    }

}
