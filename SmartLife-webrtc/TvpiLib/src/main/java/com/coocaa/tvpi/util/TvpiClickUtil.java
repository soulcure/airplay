package com.coocaa.tvpi.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.module.runtime.AppRuntime;
import com.coocaa.tvpi.module.runtime.MPRuntime;
import com.coocaa.tvpi.module.upgrade.CallUpgradeActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import swaiotos.runtime.Applet;
import swaiotos.runtime.RuntimeVersion;

import static swaiotos.runtime.Applet.APPLET_MP;


/**
 * @Author: yuzhan
 */
public class TvpiClickUtil {
    private static final String TAG = "TvpiClick";
    private static MPRuntime mp = new MPRuntime();

    public static boolean onClick(Context context, String action) {
        Log.d(TAG, "click : " + action);
        try {
            Uri uri = Uri.parse(action);
            if(checkApiLevel(context, uri)) {
                if(APPLET_MP.equals(uri.getScheme())) {//MP的逻辑暂时放这里
                    Applet applet = Applet.Builder.parse(uri);
                    mp.start(context, applet);
                    return true;
                }
                return AppRuntime.run(uri);
            } else {
                //版本号过低，拉起升级
                if(!callUpgrade(context)) {
                    ToastUtils.getInstance().showGlobalShort("功能暂未开放...");
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.getInstance().showGlobalShort("功能暂未开放...");
            return false;
        }
    }

    private static boolean callUpgrade(Context context) {
        if(context == null) {
            return false;
        }
        Intent intent = new Intent();
        intent.setClass(context, CallUpgradeActivity.class);
        if(! (context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkApiLevel(Context context, Uri uri) {
        int expectApiLevel = getApiLevelFromUri(uri);
        Log.d(TAG, "expectApiLevel=" + expectApiLevel + ", currentApiLevel=" + RuntimeVersion.RUNTIME_API_LEVEL);
        return RuntimeVersion.RUNTIME_API_LEVEL >= expectApiLevel;
    }

    private static int getApiLevelFromUri(Uri uri) {
        String rString = uri.getQueryParameter("runtime");
        if(!TextUtils.isEmpty(rString)) {
            try {
                Map<String, String> runtime = new Gson().fromJson(rString, new TypeToken<HashMap<String,String>>(){}.getType());
                String api_level_s = runtime.get("api_level");
                return Integer.parseInt(api_level_s);
            } catch (Exception e) {

            }
        }
        return 0;
    }
}
