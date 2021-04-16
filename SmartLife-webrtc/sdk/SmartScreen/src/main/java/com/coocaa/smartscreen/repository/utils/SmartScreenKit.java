package com.coocaa.smartscreen.repository.utils;

import android.content.Context;

public class SmartScreenKit {
    private static Context context;


    public static void setContext(Context c) {
       context = c;
    }

    public static Context getContext() {
        return context;
    }
}
