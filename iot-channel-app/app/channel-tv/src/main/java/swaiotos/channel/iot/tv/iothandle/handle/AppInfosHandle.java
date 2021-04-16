package swaiotos.channel.iot.tv.iothandle.handle;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.statemanager.common.bean.CmdData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.tv.TVChannelApplication;
import swaiotos.channel.iot.tv.iothandle.data.AppInfo;
import swaiotos.channel.iot.tv.iothandle.handle.base.BaseChannelHandle;
import swaiotos.channel.iot.utils.ThreadManager;


public class AppInfosHandle extends BaseChannelHandle {

    @Override
    protected void onHandle() {
        String param = mCmdData.param;
        String cmd = mCmdData.cmd;
        Log.d(TAG, "AppInfosHandle  cmd:" + cmd);
        Log.d(TAG, "AppInfosHandle  param:" + param);
        if (TextUtils.isEmpty(cmd))
            return;
        switch (cmd) {
            case "getAppInfos":
                ThreadManager.getInstance().ioThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<String> list = null;
                            if (!TextUtils.isEmpty(param)) {
                                list = new Gson().fromJson(param, new TypeToken<List<String>>() {
                                }.getType());

                            } else {
                                list = new ArrayList<>();
                            }
                            List<AppInfo> appList = getLocalAppList(TVChannelApplication.getContext(), list);
                            replayAppInfos(mIMMessage, appList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            default:
                break;
        }

    }


    public List<AppInfo> getLocalAppList(Context mContext, List<String> pkgs) {
        Map<String, Integer> aliasMap = new HashMap<>();
        Map<String, Integer> activMap = new HashMap<>();
        List<AppInfo> ret = new ArrayList<AppInfo>();
        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent();
        if (pkgs != null && pkgs.size() > 0) {
            for (String pkg : pkgs) {
                Log.d(TAG, "getLocalAppList  pkg:" + pkg);
                try {
                    AppInfo info = new AppInfo();
                    ApplicationInfo aInfo = pm.getApplicationInfo(pkg, 0);
                    info.pkgName = pkg;
                    info.className = getLauncherActivityByPkg(pm, pkg);
                    info.flag = aInfo.flags;
                    try {
                        PackageInfo pInfo = pm.getPackageInfo(pkg, 0);
                        info.firstInstallTime = pInfo.firstInstallTime;
                        info.versionCode = pInfo.versionCode;
                        info.versionName = pInfo.versionName;
                    } catch (Exception e) {

                    }
                    String lable = "";
                    try {
                        lable = aInfo.loadLabel(pm).toString();
                    } catch (Exception e) {
                    }
                    info.appName = lable;
                    ret.add(info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfo = pm.queryIntentActivities(intent, PackageManager.GET_DISABLED_COMPONENTS
                    | PackageManager.GET_RESOLVED_FILTER);
            Log.d(TAG, "getLocalAppList  resolveInfo:" + resolveInfo.size());
            if (resolveInfo != null && resolveInfo.size() > 0) {
                int si = resolveInfo.size();
                for (int i = 0; i < si; i++) {
                    ResolveInfo info = resolveInfo.get(i);
                    String pkg = info.activityInfo.packageName;
                    String className = info.activityInfo.name;
                    String targetActivity = info.activityInfo.targetActivity;

                    if (TextUtils.isEmpty(pkg) || TextUtils.isEmpty(className))
                        continue;

                    if (!TextUtils.isEmpty(targetActivity)) {
                        if (aliasMap.containsKey(pkg) || activMap.containsKey(pkg)) {
                            continue;
                        } else {
                            aliasMap.put(pkg, 1);
                        }
                    } else {
                        activMap.put(pkg, 1);
                    }

                    AppInfo aInfo = resolveInfo2LocalAppDataV2(info, className, pm);
                    if (!TextUtils.isEmpty(aInfo.pkgName)) {
                        ret.add(aInfo);
                    }
                }
            }
        }
        return ret;
    }

    private String getLauncherActivityByPkg(PackageManager pm, String packageName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        List<ResolveInfo> resolveInfo = pm.queryIntentActivities(intent, PackageManager.GET_DISABLED_COMPONENTS);
        if (resolveInfo != null && resolveInfo.size() > 0) {
            ResolveInfo info = resolveInfo.get(0);
            return info.activityInfo.name;
        }
        return "";
    }

    private static AppInfo resolveInfo2LocalAppDataV2(ResolveInfo i, String className, PackageManager pm) {
        AppInfo info = new AppInfo();
        ApplicationInfo aInfo = i.activityInfo.applicationInfo;
        info.pkgName = i.activityInfo.packageName;
        info.className = className;
        info.flag = aInfo.flags;
        try {
            PackageInfo pInfo = pm.getPackageInfo(i.activityInfo.packageName, 0);
            info.firstInstallTime = pInfo.firstInstallTime;
            info.versionCode = pInfo.versionCode;
            info.versionName = pInfo.versionName;
        } catch (Exception e) {

        }
        String lable;
        try {
            lable = i.loadLabel(pm).toString();
        } catch (Exception e) {
            lable = aInfo.loadLabel(pm).toString();
        }
        info.appName = lable;
        return info;
    }


    public void replayAppInfos(IMMessage iMessage, List<AppInfo> appList) {
        try {
            CmdData data = new CmdData("", CmdData.CMD_TYPE.APP_INFOS.toString(), new Gson().toJson(appList));
            String content = data.toJson();
            Log.d(TAG, " replayAppState content:" + content);
            IMMessage message = IMMessage.Builder.createTextMessage(iMessage.getTarget(), iMessage.getSource(), iMessage.getClientTarget(), iMessage.getClientSource(), content);
            if (mChannel != null) {
                mChannel.getIMChannel().send(message, new IMMessageCallback() {
                    @Override
                    public void onStart(IMMessage message) {
                    }

                    @Override
                    public void onProgress(IMMessage message, int progress) {
                    }

                    @Override
                    public void onEnd(IMMessage message, int code, String info) {
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
