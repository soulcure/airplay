package com.tianci.user.api.common;

import android.annotation.TargetApi;
import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.tianci.user.api.IUser;
import com.tianci.user.api.listener.AccountListenerManager;
import com.tianci.user.api.listener.OnAccountChangedListener;
import com.tianci.user.api.utils.ContextUtils;
import com.tianci.user.api.utils.JsonUtils;
import com.tianci.user.api.utils.ULog;
import com.tianci.user.api.utils.Utils;

import java.util.Map;

public class UserCommonImpl implements IUser {
    private static final String TAG = "UserCommonImpl";
    private Context context;

    public UserCommonImpl() {
        this.context = ContextUtils.get();
    }

    @Override
    public boolean hasLogin() {
        byte[] result = getDataFromProvider(ProviderConst.TYPE_LOGIN);
        return Utils.getBoolFromByte(result);
    }

    @Override
    public Map<String, Object> getInfo() {
        byte[] result = getDataFromProvider(ProviderConst.TYPE_INFO);
        String info = Utils.getStringFromBytes(result);
        ULog.i(TAG, "getInfo: " + info);
        return JsonUtils.toMap(info);
    }

    @Override
    public String getToken() {
        byte[] result = getDataFromProvider(ProviderConst.TYPE_TOKEN);
        String tokenString = Utils.getStringFromBytes(result);
        ULog.i(TAG, "getToken: " + tokenString);
        return Utils.getJsonString(tokenString, "access_token");
    }

    @Override
    public String getToken(String type) {
        byte[] result = getDataFromProvider(ProviderConst.TYPE_TOKEN);
        if (result != null && result.length > 0) {
            type = "SESSION".equalsIgnoreCase(type) ? "session_id" : "access_token";
            Map<String, String> tokenMap = JsonUtils.toMapV0(new String(result));
            return tokenMap == null ? null : tokenMap.get(type);
        }

        return null;
    }

    @Override
    public String getSession() {
        Bundle result = call("get_session", null);
        return result == null ? "" : result.getString("result", "");
    }

    @Override
    public boolean logout() {
        Bundle extra = new Bundle();
        extra.putString("package", context.getPackageName());
        return Utils.getResult(call("logout", extra));
    }

    @Override
    public boolean loginByToken(String accessToken) {
        Bundle params = new Bundle();
        params.putString("access_token", accessToken);
        params.putString("package", context.getPackageName());
        return Utils.getResult(call("login_by_token", params));
    }

    @Override
    public boolean isPublicAddress() {
        Bundle result = call("is_public_address", null);
        return result == null || result.getBoolean("result", true); // 默认true
    }

    @Override
    public boolean refreshAccountInfo() {
        Bundle result = call("refresh_account_info", null);
        return Utils.getResult(result);
    }

    @Override
    public boolean registerAccountChanged(OnAccountChangedListener listener) {
        return AccountListenerManager.getInstance(context).register(listener);
    }

    @Override
    public boolean unregisterAccountChanged(OnAccountChangedListener listener) {
        return AccountListenerManager.getInstance(context).unregister(listener);
    }

    private Bundle call(String method, Bundle params) {
        ULog.i(TAG, "call, method = " + method);

        try {
            return getClient(context, ProviderConst.getUri()).call(method, null, params);
            // return context.getContentResolver().call(ProviderConst.getUri(), method, null, params);
        } catch (Exception e) {
            ULog.e(TAG, "call(), exception = " + e.getMessage());
            e.printStackTrace();
        }

        return Bundle.EMPTY;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private byte[] getDataFromProvider(String type) {
        ULog.i(TAG, "getDataFromProvider, type = " + type);
        Cursor cursor = null;
        try {
            cursor = getClient(context, ProviderConst.getUri())
                    .query(ProviderConst.getUri(), null, ProviderConst.getSelectionByType(type),
                            null, null, null);
            /*
            cursor = context.getContentResolver()
                    .query(ProviderConst.getUri(), null, ProviderConst.getSelectionByType(type),
                            null, null, null);
             */
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getBlob(cursor.getColumnIndex(ProviderConst._INFO));
            }
        } catch (Exception e) {
            ULog.e(TAG, "getDataFromProvider, exception = " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // ULog.i(TAG, "getDataFromProvider, return null");
        return null;
    }

    private ContentProviderClient getClient(Context context, Uri uri)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            return context.getContentResolver().acquireUnstableContentProviderClient(uri);
        } else
        {
            return context.getContentResolver().acquireContentProviderClient(uri);
        }
    }
}
