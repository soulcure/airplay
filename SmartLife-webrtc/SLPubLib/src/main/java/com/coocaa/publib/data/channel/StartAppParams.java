package com.coocaa.publib.data.channel;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @ClassName StartAppParams
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/9
 * @Version TODO (write something)
 */
public class StartAppParams {

    /**
     * 用于给CmdData cmd字段赋值
     */
    public enum CMD{
        LIVE_VIDEO,
        ONE_KEY_CLEAR,
        PREVIEW_SCREENSAVER,
        CUSTOM_SCREENSAVER
    }

    public static final String DOWHAT_START_ACTIVITY = "startActivity";
    public static final String DOWHAT_START_SERVICE = "startService";
    public static final String DOWHAT_SEND_BROADCAST = "sendBroadcast";
    public static final String DOWHAT_SEND_INTERNALBROADCAST = "sendInternalBroadcast";

    public static final String BYWHAT_ACTION = "action";
    public static final String BYWHAT_CLASS = "class";
    public static final String BYWHAT_URI = "uri";

    public String packagename;
    public int versioncode;
    public String dowhat;  //startService  startActivity  sendBroadcast
    public String bywhat;   //action  class
    public String byvalue;  //className
    public String data;
    public Map<String, String> params;
    public StartAppParams exception;


    public Intent buildIntent(Context c) {
        Intent intent = null;
        if (bywhat != null && !bywhat.equals("") && !bywhat.equals("null")) {
            intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!TextUtils.isEmpty(packagename)) {
                intent.setPackage(packagename);
            }
            if (bywhat.equals(BYWHAT_ACTION) && !TextUtils.isEmpty(byvalue)) {
                intent.setAction(byvalue);
                if (!TextUtils.isEmpty(data))
                    intent.setData(Uri.parse(data));
            } else if (bywhat.equals(BYWHAT_CLASS) && !TextUtils.isEmpty(byvalue)) {
                intent.setClassName(packagename, byvalue);
                if (!TextUtils.isEmpty(data))
                    intent.setData(Uri.parse(data));
            } else if (bywhat.equals(BYWHAT_URI) && !TextUtils.isEmpty(byvalue)) {
                intent.setData(Uri.parse(byvalue));
            } else {
                byvalue = getLauncherActivity(c, packagename);
                if (!TextUtils.isEmpty(byvalue))
                    intent.setClassName(packagename, byvalue);
            }
        } else if (!TextUtils.isEmpty(packagename)) {
            intent = new Intent();
            intent.setPackage(packagename);
            byvalue = getLauncherActivity(c, packagename);
            if (!TextUtils.isEmpty(byvalue))
                intent.setClassName(packagename, byvalue);
        }
        if (intent != null && params != null && params.size() > 0) {
            Iterator iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                intent.putExtra((String) entry.getKey(), (String) entry.getValue());
            }
        }
        return intent;
    }

    private static String getLauncherActivity(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        List<ResolveInfo> resolveInfo = pm.queryIntentActivities(intent, PackageManager.GET_DISABLED_COMPONENTS);
        if (resolveInfo != null && resolveInfo.size() > 0) {
            ResolveInfo info = resolveInfo.get(0);
            return info.activityInfo.name;
        }
        return "";
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

}
