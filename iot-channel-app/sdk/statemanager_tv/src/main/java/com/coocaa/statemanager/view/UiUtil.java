package com.coocaa.statemanager.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;

/**
 * Created by yellowlgx on 2015/7/21.
 */
public class UiUtil {
    private static float mDiv = 1.0f;
    private static float mDpi = 1.0f;

    public static void init(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mDiv = (float) dm.widthPixels / 1920.0f;
        mDpi = mDiv / dm.density;
    }

    public static int div(int x) {
        return (int) (x * mDiv + 0.5f);
    }

    public static int dpi(int x) {
        return (int) (x * mDpi + 0.5f);
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 产生shape类型的drawable
     *
     * @param solidColor  主颜色值
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度
     * @param radius      椭圆半径
     * @return
     */
    public static GradientDrawable getDrawable(int solidColor, int strokeColor, int strokeWidth, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(solidColor);
        drawable.setStroke(strokeWidth, strokeColor);
        drawable.setCornerRadius(radius);
        return drawable;
    }
}
