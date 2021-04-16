package com.coocaa.publib.utils;

import android.util.Log;

import com.coocaa.publib.PublibHelper;

public class IRLog {

    public IRLog() {
    }

    public static void i(String tag, String msg) {
        if (PublibHelper.PrintLog) {
            Log.i(tag, msg);
        }

    }

    public static void e(String tag, String msg) {
        if (PublibHelper.PrintLog) {
            Log.e(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (PublibHelper.PrintLog) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (PublibHelper.PrintLog) {
            Log.d(tag, msg);
        }
    }
}
