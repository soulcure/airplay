package com.coocaa.tvpi.util;

import android.view.DisplayCutout;
import android.view.Window;
import android.view.WindowInsets;

import androidx.annotation.RequiresApi;

public class NotchUtil {


    @RequiresApi(api = 28)
    public static boolean isNotchScreen(Window window) {
        WindowInsets windowInsets = window.getDecorView().getRootWindowInsets();
        if (windowInsets == null) {
            return false;
        }

        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        if(displayCutout == null || displayCutout.getBoundingRects() == null){
            return false;
        }
        return true;
    }


    @RequiresApi(api = 28)
    public static int getNotchHeight(Window window) {
        int notchHeight = 0;
        WindowInsets windowInsets = window.getDecorView().getRootWindowInsets();
        if (windowInsets == null) {
            return 0;
        }

        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        if(displayCutout == null || displayCutout.getBoundingRects() == null){
            return 0;
        }

        notchHeight = displayCutout.getSafeInsetTop();

        return notchHeight;
    }
}
