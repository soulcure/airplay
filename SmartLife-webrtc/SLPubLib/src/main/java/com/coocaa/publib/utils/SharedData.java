/**
 * Copyright (C) 2012 The SkyTvOS Project
 *
 * Version     Date           Author
 * ─────────────────────────────────────
 *           2014年7月4日         Chenglong
 *
 */

package com.coocaa.publib.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @ClassName SharedData
 * @Description 保存各种数据，包括记录上次打开的页面以及更多中的设置选项
 * @author Chenglong
 * @date 2014年7月4日
 */
public class SharedData {
	private SharedPreferences preference;
	private SharedPreferences.Editor editor;

	private static final String TVPI_SP = "TVPI_SP";

	public static class Keys{
		public final static String COOCAA_TOKEN = "COOCAA_TOKEN";
		public final static String LOGIN_USER_NAME = "USER_NAME";

		// 连接上电视后，保存对应的mac model version，如果登录时还没有断开，则将信息上报给服务器； 断开了则清除掉本地的这些信息
        public final static String MAC_DEVICE_CONNECT = "MAC_DEVICE_CONNECT";
        public final static String MODEL_DEVICE_CONNECT = "MODEL_DEVICE_CONNECT";
        public final static String VERSION_DEVICE_CONNECT = "VERSION_DEVICE_CONNECT";
        public final static String TVNAME_DEVICE_CONNECT = "TVNAME_DEVICE_CONNECT";
	}

	private static SharedData sInstance;

	public static SharedData getInstance() {
		return sInstance;
	}

	// 必须在myapplication中初始化
	public static SharedData init(Context context) {
		if (sInstance == null) {
			sInstance = new SharedData(context);
		}
		return sInstance;
	}

	public SharedData(Context context) {
		preference = context.getSharedPreferences(TVPI_SP, Context.MODE_PRIVATE);
		editor = preference.edit();
	}

	public void putInt(String key, int value) {
		editor.putInt(key, value).commit();
	}

	public void putString(String key, String value) {
		editor.putString(key, value).commit();
	}

	public void putLong(String key, long value) {
		editor.putLong(key, value).commit();
	}

	public int getInt(String key, int dValue) {
		return preference.getInt(key, dValue);
	}

	public String getString(String key, String dValue) {
		return preference.getString(key, dValue);
	}

	public long getLong(String key, long dValue) {
		return preference.getLong(key, dValue);
	}

	public boolean getBoolean(String key, boolean dValue) {
		return preference.getBoolean(key, dValue);
	}

	public void putBoolean(String key, boolean dValue) {
		editor.putBoolean(key, dValue).commit();
	}
}
