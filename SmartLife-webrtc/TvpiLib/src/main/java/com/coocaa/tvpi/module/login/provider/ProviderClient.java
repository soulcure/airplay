package com.coocaa.tvpi.module.login.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.repository.utils.SmartScreenKit;
import com.google.gson.Gson;
import com.tianci.user.api.common.ProviderConst;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ProviderClient
{
    private static class Holder
    {
        public static final ProviderClient client = new ProviderClient();
    }

    private ProviderClient()
    {
    }

    public static ProviderClient getClient()
    {
        return Holder.client;
    }

    private static final String TAG = "ProviderClient";

    /**
     * 同步账户信息到ContentProvider
     *
     * @param token
     *         access token
     * @param infoJson
     *         账户信息
     * @return 同步结果
     */
    public boolean saveInfo(String token, String infoJson)
    {
        if (TextUtils.isEmpty(token))
        {
            Log.e(TAG, "saveInfo(), token is null or empty");
            return false;
        }
        if (TextUtils.isEmpty(infoJson))
        {
            Log.e(TAG, "saveInfo(), info is null or empty");
            return false;
        }

        insertUnique(getContext(), ProviderConst.TYPE_LOGIN, String.valueOf(true).getBytes());
        // Log.i(TAG, "saveInfo(), token = " + token);
        insertUnique(getContext(), ProviderConst.TYPE_TOKEN, createToken(token).getBytes());
        // Log.i(TAG, "saveInfo(), infoJson = " + infoJson);
        insertUnique(getContext(), ProviderConst.TYPE_INFO, infoJson.getBytes());

        return true;
    }

    private String createToken(String accessToken)
    {
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("access_token", accessToken);
        tokenMap.put("session_id", Integer.toHexString((new Random()).nextInt()) + ":0123456789AB");
        return new Gson().toJson(tokenMap);
    }

    public boolean clear()
    {
        try
        {
            ContentResolver resolver = getContext().getContentResolver();
            int result = resolver.delete(Uri.parse(ProviderConst.CONTENT_PATH), null, null);
            Log.i(TAG, "clear(), result = " + result);
            return true;
        } catch (Exception e)
        {
            Log.i(TAG, "clear(), exception = " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private boolean insertUnique(Context context, String type, byte[] data)
    {
        Cursor cursor = null;
        try
        {
            ContentValues values = constructContentValues(type, data);
            ContentResolver resolver = context.getContentResolver();
            String selection = getSelectionByType(type);
            cursor = resolver.query(getUri(), null, selection, null, null);
            if (cursor != null && cursor.getCount() > 0)
            {
                int result = resolver.update(getUri(), values, selection, null);
                Log.d(TAG, "insertUnique(), update result = " + result);
            } else
            {
                Uri uriResult = resolver.insert(getUri(), values);
                //                VLog.d(TAG, "insertUnique(), result = " + uriResult);
            }

            return true;
        } catch (Exception e)
        {
            Log.e(TAG, "insertUnique(), exception = " + e.getMessage());
            e.printStackTrace();
        } finally
        {
            if (cursor != null)
            {
                try
                {
                    cursor.close();
                } catch (Exception e)
                {
                }
            }
        }

        return false;
    }

    private ContentValues constructContentValues(String type, byte[] data)
    {
        ContentValues values = new ContentValues();
        values.put(ProviderConst._TYPE, type);
        values.put(ProviderConst._INFO, data);
        return values;
    }

    private String getSelectionByType(String type)
    {
        return ProviderConst.getSelectionByType(type);
    }

    private Uri getUri()
    {
        return ProviderConst.getUri();
    }

    private Context getContext()
    {
        return SmartScreenKit.getContext();
    }
}
