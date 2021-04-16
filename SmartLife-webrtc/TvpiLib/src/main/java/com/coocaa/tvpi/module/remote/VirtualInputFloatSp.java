package com.coocaa.tvpi.module.remote;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @Author: yuzhan
 */
public class VirtualInputFloatSp implements SharedPreferences {

    @Override
    public Map<String, ?> getAll() {
        return null;
    }

    @Nullable
    @Override
    public String getString(String s, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String s, @Nullable Set<String> set) {
        return null;
    }

    @Override
    public int getInt(String s, int i) {
        return 0;
    }

    @Override
    public long getLong(String s, long l) {
        return 0;
    }

    @Override
    public float getFloat(String s, float v) {
        return 0;
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        return false;
    }

    @Override
    public boolean contains(String s) {
        return false;
    }

    @Override
    public Editor edit() {
        return null;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    private static class SharedPreferenceProvider extends ContentProvider {

        @Override
        public boolean onCreate() {
            return false;
        }

        @Nullable
        @Override
        public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
            return null;
        }

        @Nullable
        @Override
        public String getType(@NonNull Uri uri) {
            return null;
        }

        @Nullable
        @Override
        public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
            return null;
        }

        @Override
        public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
            return 0;
        }

        @Override
        public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
            return 0;
        }
    }

    private static class VirtualInputFloatEditor implements Editor {

        @Override
        public Editor putString(String s, @Nullable String s1) {
            return null;
        }

        @Override
        public Editor putStringSet(String s, @Nullable Set<String> set) {
            return null;
        }

        @Override
        public Editor putInt(String s, int i) {
            return null;
        }

        @Override
        public Editor putLong(String s, long l) {
            return null;
        }

        @Override
        public Editor putFloat(String s, float v) {
            return null;
        }

        @Override
        public Editor putBoolean(String s, boolean b) {
            return null;
        }

        @Override
        public Editor remove(String s) {
            return null;
        }

        @Override
        public Editor clear() {
            return null;
        }

        @Override
        public boolean commit() {
            return false;
        }

        @Override
        public void apply() {

        }
    }
}
