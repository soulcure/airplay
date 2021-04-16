package com.coocaa.define;

import android.graphics.Color;

public class SvgConfig {

    public static String BG_COLOR_STRING = "#E5E5E5";
    public static int BG_COLOR = Color.parseColor(BG_COLOR_STRING);

    public static void changeToClient() {
        BG_COLOR_STRING = "#FFFFFF";
        BG_COLOR = Color.parseColor(BG_COLOR_STRING);
    }

    public static void changeColor(String color) {
        BG_COLOR_STRING = color;
        BG_COLOR = Color.parseColor(color);
    }
}
