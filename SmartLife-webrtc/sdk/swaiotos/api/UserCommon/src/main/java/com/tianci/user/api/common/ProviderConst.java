package com.tianci.user.api.common;

import android.net.Uri;

public class ProviderConst {
    public static String _TYPE = "type";
    public static String _INFO = "info";
    public static final String AUTHORITY = "com.tianci.user.UserProvider";  //授权
    public static final String CONTENT_PATH = "content://com.tianci.user.UserProvider/account";
    // public static String[] projection = new String[]{_INFO};
    public static String TYPE_INFO = "info";
    public static String TYPE_TOKEN = "token";
    public static String TYPE_LOGIN = "login";

    public static String getSelectionByType(String type) {
        return ProviderConst._TYPE + "='" + type + "'";
    }

    public static Uri getUri() {
        return Uri.parse(ProviderConst.CONTENT_PATH);
    }
}
