package com.coocaa.tvpi.module.homepager.adapter.bean;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class HeaderFunctionBean {
    public static final int FUNCTION_MIRROR = 0;
    public static final int FUNCTION_MOVIE = 1;
    public static final int FUNCTION_LIVE = 2;
    public static final int FUNCTION_APP = 3;
    public static final int FUNCTION_LOCAL_PUSH = 4;
    public static final int FUNCTION_VIDEO_CALL = 5;
    public static final int FUNCTION_HOME_MONITOR = 6;
    public static final int FUNCTION_MESSAGE = 7;
    public static final int FUNCTION_IQIYI = 8;
    public static final int FUNCTION_QQ = 9;
    public static final int FUNCTION_REVERSE_SCREEN = 10;
    public static final int FUNCTION_SHORTCUT= 11;
    public static final int FUNCTION_KUKAI_TV= 12;

    @IntDef({FUNCTION_MIRROR, FUNCTION_MOVIE, FUNCTION_LIVE, FUNCTION_APP,
            FUNCTION_LOCAL_PUSH, FUNCTION_VIDEO_CALL, FUNCTION_HOME_MONITOR, FUNCTION_MESSAGE,
            FUNCTION_IQIYI, FUNCTION_QQ,FUNCTION_REVERSE_SCREEN,FUNCTION_SHORTCUT,FUNCTION_KUKAI_TV})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FunctionType {
    }

    public int icon;
    public String name;
    public int type = 0; //0普通视图类型 1.用于镜像中的类型
    public @FunctionType int functionType;

    @Override
    public String toString() {
        return "HeaderFunctionBean{" +
                "icon=" + icon +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", functionType=" + functionType +
                '}';
    }
}
