package swaiotos.runtime.h5.core.os.exts.utils;

import android.util.Log;

public class ExtLog {
    private static final String TAG = "ExtLog";

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        Log.d(TAG, tag + " - " + msg);
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(TAG, tag + " - " + msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + " - " + msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }


    public static void e(String tag, String msg) {
        Log.e(TAG, tag + " - " + msg);
    }

}
