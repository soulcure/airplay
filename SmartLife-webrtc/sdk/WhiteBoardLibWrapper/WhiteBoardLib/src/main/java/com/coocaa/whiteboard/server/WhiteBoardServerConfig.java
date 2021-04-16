package com.coocaa.whiteboard.server;

public class WhiteBoardServerConfig {

    //默认x初始位置
    private final static int DEFAULT_OFFSET_X = 1920;
    //默认y初始位置
    private final static int DEFAULT_OFFSET_Y = 1080;

    //画布大小，宽度
    public static int MAX_WIDTH = 1920 * 3;

    //画布大小，高度
    public static int MAX_HEIGHT = 1080 * 3;

    private final static float DEFAULT_SCALE = 1f/3f;//默认缩放比例


    public static int CURRENT_OFFSET_X = DEFAULT_OFFSET_X;
    public static int CURRENT_OFFSET_Y = DEFAULT_OFFSET_Y;

    public static float CURRENT_SCALE = DEFAULT_SCALE;

    public static void setMax(int w, int h) {
        MAX_WIDTH = w;
        MAX_HEIGHT = h;
    }

    public static void setScale(float s) {
        CURRENT_SCALE = s;
    }
}
