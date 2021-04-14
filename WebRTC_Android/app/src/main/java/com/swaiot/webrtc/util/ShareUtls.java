package com.swaiot.webrtc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ShareUtls {
	private SharedPreferences sp;
	private SharedPreferences.Editor spe;
	/* 配置文件键 */
	private final String PREF_FILE_NAME = "swaiot_devicemanager";

	public static ShareUtls _instance;
	private ShareUtls(Context ct) {
		sp = ct.getSharedPreferences(PREF_FILE_NAME,
				Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		spe = sp.edit();
	}

	public static ShareUtls getInstance(Context context) {
		if (_instance == null) {
			_instance = new ShareUtls(context);
		}
		return _instance;
	}

	public void putBoolean(String key, boolean value) {
		Log.e(key, "" + value);
		spe.putBoolean(key, value).commit();
	}

	public void putString(String key, String value) {
		Log.e(key, "SharePref-put:" + value);
		spe.putString(key, value).commit();
	}

	public void putInt(String key, int value) {
		spe.putInt(key, value).commit();
	}

	public boolean getBoolean(String key, boolean defValue) {
		boolean value;
		value = sp.getBoolean(key, defValue);
		Log.e(key, "SharePref:" + value);
		return sp.getBoolean(key, defValue);
	}

	public String getString(String key, String defValue) {
		String value;
		value = sp.getString(key, defValue);
		Log.e(key, "SharePref-get:" + value);
		return sp.getString(key, defValue);
	}

	public int getInt(String key, int defValue) {
		return sp.getInt(key, defValue);
	}

	public void remove(String key) {
		spe.remove(key).commit();
	}

}
