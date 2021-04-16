package com.coocaa.tvpi.module.runtime;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.net.Uri;

import swaiotos.runtime.AppletRuntimeManager;

/**
 * @Author: yuzhan
 */
public class AppRuntime {
    private static AppletRuntimeManager manager;

    public static void init(Context context) {
        if(manager == null) {
            if(context instanceof Activity || context instanceof Service) {
                manager = AppletRuntimeManager.get(context.getApplicationContext());
            } else {
                manager = AppletRuntimeManager.get(context);
            }
        }
    }

    public static boolean run(Uri uri) {
        try {
            return manager.startApplet(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
