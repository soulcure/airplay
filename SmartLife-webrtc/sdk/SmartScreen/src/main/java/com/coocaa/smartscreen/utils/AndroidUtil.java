package com.coocaa.smartscreen.utils;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @Author: yuzhan
 */
public class AndroidUtil {

    private static Context appContext;

    public static void setAppContext(Context context) {
        if(context instanceof Activity || context instanceof Service)
            appContext = context.getApplicationContext();
        else
            appContext = context;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static String readAssetFile(String assetFile) {
        if(appContext == null || TextUtils.isEmpty(assetFile))
            return null;

        StringBuilder sb = new StringBuilder();
        AssetManager assetManager = appContext.getAssets();
        try {
            InputStream is = assetManager.open(assetFile, AssetManager.ACCESS_BUFFER);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void printException(String log_tag, String msg, Throwable e) {
        if(e == null || log_tag == null)
            return ;
        StringBuilder sb = new StringBuilder();
        sb.append("Exception. msg: ");
        if(msg != null)
            sb.append(msg);
        else if(!TextUtils.isEmpty(e.getMessage()))
            sb.append(e.getMessage());
        sb.append(":\n");
        StackTraceElement[] trace = e.getStackTrace();
        try {
            for (StackTraceElement traceElement : trace) {
                sb.append("\tat ");
                sb.append(traceElement);
                sb.append("\n");
            }
        } catch (Exception e2) {
            e.printStackTrace();
        }
        if(e.getCause() != null) {
            StackTraceElement[] causeStackTrace = e.getCause().getStackTrace();
            if(causeStackTrace != null) {
                sb.append("\nCaused by:\n");
                try {
                    for (StackTraceElement traceElement : causeStackTrace) {
                        sb.append("\tat ");
                        sb.append(traceElement);
                        sb.append("\n");
                    }
                } catch (Exception e2) {
                    e.printStackTrace();
                }
            }
        }
        android.util.Log.e(log_tag, sb.toString());
    }
}
