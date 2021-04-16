package com.coocaa.tvpi.util;

import android.content.Context;

public class DisplayMetricsUtils {
    public static int getPx(Context context, int dp){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp* scale + 0.5f);
    }
}
