package com.tianci.user.api.utils;

import android.util.Log;

import com.tianci.user.api.Defines;

public class ULog {
    private static final String TAG = "UserApi";

    public static void i(String msg) {
        if (Defines.DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (Defines.DEBUG) {
            Log.i(TAG, tag + " - " + msg);
        }
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + " - " + msg);
    }
}
