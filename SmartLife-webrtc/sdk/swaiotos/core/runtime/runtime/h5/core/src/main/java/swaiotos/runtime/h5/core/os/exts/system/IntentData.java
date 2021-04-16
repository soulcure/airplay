package swaiotos.runtime.h5.core.os.exts.system;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import swaiotos.runtime.Applet;
import swaiotos.runtime.h5.H5AppletRunner;
import swaiotos.runtime.h5.core.os.exts.utils.ExtLog;

/**
 * @Author: yuzhan
 */
public class IntentData implements Serializable {

    public static final String TYPE_START_ACTIVIY = "activity";
    public static final String TYPE_START_SERVICE = "service";
    public static final String TYPE_SEND_BROADCAST = "broadcast";

    public static final String MODE_ACTION = "action";
    public static final String MODE_CLASS = "class";
    public static final String MODE_URI = "uri";
    public static final String MODE_PKG = "package";

    public String type = TYPE_START_ACTIVIY;

    //启动方式，分别有[action|class|uri|package]
    public String mode;

    //根据启动方式指定具体的值，可以是action名、类名、URI字串
    public String value;

    //应用包名，只有mode为action/uri时可不填
    public String packageName;

    //版本号，用来判断是否需要升级应用，默认不需要升级可以填0
    public String versionCode;

    public String params;

    private Map<String, String> paramsMap;

    private Intent buildIntent(Context context) {
        ExtLog.d("start buildIntent, data=" + this);
        Intent intent = new Intent();
        if(!TextUtils.isEmpty(packageName)) {
            intent.setPackage(packageName);
        }
        parseMode(context, intent);
        parseParams(intent);
        return intent;
    }

    public boolean start(Context context) {
        if(TextUtils.isEmpty(type)) {
            return false;
        }
        Intent intent = buildIntent(context);
        ExtLog.d("start, intent=" + intent);
        switch (type) {
            case TYPE_START_ACTIVIY:
                startActivit(context, intent);
                break;
            case TYPE_START_SERVICE:
                try {
                    context.startService(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                break;
            case TYPE_SEND_BROADCAST:
                context.sendBroadcast(intent);
                break;
        }
        return true;
    }

    private boolean startActivit(Context context, Intent intent) {
        if(! (context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        boolean isH5 = false;
        if(intent.getComponent() == null && intent.getData() != null) {
            //判断是否是h5
            if(Applet.APPLET_H.contains(intent.getData().getScheme())) {
                isH5 = true;
            }
        }
        if(isH5) {
            try {
                H5AppletRunner.get().start(context, Applet.Builder.parse(intent.getData()));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private void parseMode(Context context, Intent intent) {
        if(!TextUtils.isEmpty(mode)) {
            switch (mode) {
                case MODE_PKG:
                    intent.setClassName(packageName, getLauncherActivity(context, packageName));
                    break;
                case MODE_CLASS:
                    intent.setClassName(packageName, value);
                    break;
                case MODE_URI:
                    intent.setData(Uri.parse(value));
                    break;
                case MODE_ACTION:
                    intent.setAction(value);
                    break;
            }
        }
    }

    private void parseParams(Intent intent) {
        if(paramsMap == null && !TextUtils.isEmpty(params)) {
            try {
                paramsMap = JSONObject.parseObject(params, new TypeReference<HashMap<String, String>>(){});
            } catch (Exception e) {
            }
        }
        if(paramsMap != null && !paramsMap.isEmpty()) {
            Iterator<Map.Entry<String, String>> iter = paramsMap.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
    }

    private static String getLauncherActivity(Context context, String packageName) {
        Log.d("CCC", "getLauncherActivity, pkg=" + packageName);
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        List<ResolveInfo> resolveInfo =
                pm.queryIntentActivities(intent, PackageManager.GET_DISABLED_COMPONENTS);
        if (resolveInfo != null && !resolveInfo.isEmpty()) {
            for(ResolveInfo info :resolveInfo) {
                Log.d("CCC", "resolve launcher : " + info.resolvePackageName + "/" + info.activityInfo.name);
            }
            ResolveInfo info = resolveInfo.get(0);
            return info.activityInfo.name;
        }
        return "";
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("IntentData{");
        sb.append("type='").append(type).append('\'');
        sb.append(", mode='").append(mode).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", packageName='").append(packageName).append('\'');
        sb.append(", versionCode='").append(versionCode).append('\'');
        sb.append(", params='").append(params).append('\'');
        sb.append(", paramsMap=").append(paramsMap);
        sb.append('}');
        return sb.toString();
    }
}
