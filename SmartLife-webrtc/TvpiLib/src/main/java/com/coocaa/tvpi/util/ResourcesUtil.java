package com.coocaa.tvpi.util;

import android.content.Context;

import androidx.annotation.StringRes;

/**
 * @author kangwen
 * @date 2020/9/9.
 */
public class ResourcesUtil {

    /**
     * 根据 id 获取一个文本
     */
    public static String getString(Context context, @StringRes int id) {
        return context.getString(id);
    }

    public static String getString(Context context, @StringRes int id, Object... formatArgs) {
        return context.getResources().getString(id, formatArgs);
    }
}
