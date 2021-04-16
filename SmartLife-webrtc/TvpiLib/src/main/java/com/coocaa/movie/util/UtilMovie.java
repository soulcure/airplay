//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.coocaa.movie.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class UtilMovie {
    private static UtilMovie mUtil;
    private static DisplayMetrics dm;
    private static float mDiv = 1.0F;
    private static float mDpi = 1.0F;

    public UtilMovie() {
    }

    public static UtilMovie instence(Context context) {
        if (mUtil == null) {
            mUtil = new UtilMovie();
        }

        if (dm == null) {
            dm = context.getResources().getDisplayMetrics();
        }

        mDiv = (float)dm.widthPixels / 375.0F;
        mDpi = mDiv / dm.density;
        return mUtil;
    }

    public static UtilMovie instance() {
        return mUtil;
    }

    public DisplayMetrics getDm() {
        return dm;
    }

    public static int Div(int x) {
        return (int)((float)x * mDiv);
    }

    public static int Dpi(int x) {
        return (int)((float)x * mDpi);
    }

    public static float getScaleRatio(int w) {
        return w <= Div(500) ? 1.05F : (float)((double)(w + Div(40)) * 1.0D / (double)w);
    }
}
