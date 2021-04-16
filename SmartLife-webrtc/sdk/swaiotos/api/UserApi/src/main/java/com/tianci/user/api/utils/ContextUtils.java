package com.tianci.user.api.utils;

import android.content.Context;

public class ContextUtils {
    private static Context appContext;

    public static Context init(Context context) {
        if (context == null) {
            throw new NullPointerException("Context is null");
        }

        appContext = context.getApplicationContext();
        appContext = appContext == null ? context : appContext;
        return appContext;
    }

    public static Context get() {
        return appContext;
    }
}
