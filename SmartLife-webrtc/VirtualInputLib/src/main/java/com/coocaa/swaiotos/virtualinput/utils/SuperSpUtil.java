package com.coocaa.swaiotos.virtualinput.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.coocaa.swaiotos.virtualinput.utils.supersp.SuperSpProxy;

import androidx.annotation.NonNull;

/**
 * @Author: yuzhan
 */
public class SuperSpUtil {

    public static final String METHOD_CONTAIN_KEY = "method_contain_key";
    public static final String AUTHORITY = "com.coocaa.smartscreen.superpreference";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY);
    public static final String METHOD_QUERY_VALUE = "method_query_value";
    public static final String METHOD_EIDIT_VALUE = "method_edit";
    public static final String METHOD_QUERY_PID = "method_query_pid";
    public static final String KEY_VALUES = "key_result";
    private static final String NAME_SPACE = "SuperSp";

    public static final Uri sContentCreate = Uri.withAppendedPath(URI, "create");

    public static final Uri sContentChanged = Uri.withAppendedPath(URI, "changed");

    /**
     * 获取long值
     *
     * @param context  上下文
     * @param key      键
     * @param defValue 默认值
     * @return 保存的值
     */
    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = getSharedPreference(context);
        return sp.getInt(key, defValue);
    }

    /**
     * 保存int值
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp = getSharedPreference(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * 存入字符串
     *
     * @param context 上下文
     * @param key     字符串的键
     * @param value   字符串的值
     */
    public static void putString(Context context, String key, String value) {
        SharedPreferences preferences = getSharedPreference(context);
        //存入数据
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 获取字符串
     *
     * @param context 上下文
     * @param key     字符串的键
     * @return 得到的字符串
     */
    public static String getString(Context context, String key) {
        SharedPreferences preferences = getSharedPreference(context);
        return preferences.getString(key, "");
    }


    public static void clear(Context context, String key) {
        SharedPreferences sp = getSharedPreference(context);
        if(sp.contains(key)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(key);
            editor.commit();
        }
    }

    private static SharedPreferences getSharedPreference(@NonNull Context ctx) {
        return SuperSpProxy.getSharedPreferences(ctx, NAME_SPACE);
    }
}
