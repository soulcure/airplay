package com.coocaa.statemanager.view;

import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.widget.TextView;

/**
 * @ Created on: 2020/9/3
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class ThemeUtils {

    public static GradientDrawable getDrawable(@ColorInt int Color, float[] radius, int strokeColor, int strokeWidth) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color);
        gradientDrawable.setStroke(strokeWidth, strokeColor);
        gradientDrawable.setCornerRadii(radius);
        return gradientDrawable;
    }

    public static GradientDrawable getDrawable(@ColorInt int color, float radius) {
        return getDrawable(color, new float[]{radius, radius, radius, radius, radius, radius, radius, radius}, 0, 0);
    }

    public static GradientDrawable getDrawable(@ColorInt int color, float topLeftRadius,float topRightRadius,float bottomRightRadius,float bottomLeftRadius) {
        return getDrawable(color, new float[]{topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomRightRadius}, 0, 0);
    }


    public static GradientDrawable getDrawable(@ColorInt int color, float radius, int strokeColor, int strokeWidth) {
        return getDrawable(color, new float[]{radius, radius, radius, radius, radius, radius, radius, radius}, strokeColor, strokeWidth);
    }


    public static void setTextViewCondition(TextView textView, String text, int textSize, @ColorInt int color, boolean isBold) {
        textView.setTextSize(textSize);
        textView.setIncludeFontPadding(false);
        textView.setTextColor(color);
        textView.getPaint().setFakeBoldText(isBold);
        textView.setText(text);
    }
}

