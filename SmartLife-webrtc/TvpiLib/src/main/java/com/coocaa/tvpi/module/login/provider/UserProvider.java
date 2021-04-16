package com.coocaa.tvpi.module.login.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.tianci.user.api.common.ProviderConst;

public class UserProvider extends ContentProvider
{
    public static class Method
    {
        public static final String METHOD_LOGOUT = "logout";
        public static final String METHOD_GET_SESSION = "get_session";
        public static final String METHOD_LOGIN_BY_TOKEN = "login_by_token";
        public static final String METHOD_IS_PUBLIC_ADDRESS = "is_public_address";
    }

    private static final String TAG = "UserProvider";
    private static final String TABLE_NAME = DbOpenHelper.TABLE_NAME;
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int TABLE_CODE_INFO = 2;

    static
    {
        //关联不同的 URI 和 code，便于后续 getType
        mUriMatcher.addURI(ProviderConst.AUTHORITY, TABLE_NAME, TABLE_CODE_INFO);
    }

    @Override
    public boolean onCreate()
    {
        boolean initResult = initDataBase();
        Log.i(TAG, "onCreate(), initResult = " + initResult);
        return true;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras)
    {
        Log.d(TAG, "call(), method = " + method + ", arg = " + arg);
        try
        {
            switch (method)
            {
                case Method.METHOD_LOGOUT:
                {
                    ProviderClient.getClient().clear();
                    return createResult(true);
                }
                case Method.METHOD_GET_SESSION:
                {
                    String session = ""; // TODOUserManager.getInstance().getSession();
                    Bundle result = new Bundle();
                    result.putString("result", session);
                    return result;
                }
                case Method.METHOD_IS_PUBLIC_ADDRESS:
                {
                    return createResult(true);
                }
                case Method.METHOD_LOGIN_BY_TOKEN:
                {
                    return createResult(false);
                }
                default:
                {
                    Log.e(TAG, "call(), no handler method = " + method);
                }
            }
        } catch (Exception e)
        {
            Log.e(TAG, "call(), exception = " + e.getMessage());
            e.printStackTrace();
        }

        return super.call(method, arg, extras);
    }

    private Bundle createResult(boolean result)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean("result", result);
        return bundle;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
    {
        //        VLog.d(TAG, "query(), selection = " + selection);
        // VLog.i(TAG, "query(), selectionArgs = " + JsonUtils.toJson(selectionArgs));

        if (initDataBase())
        {
            return queryFromDB(uri, projection, selection, selectionArgs, sortOrder);
        }

        Log.w(TAG, "query(), initDataBase failed");
        return null;
    }

    @Override
    public String getType(Uri uri)
    {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        Log.d(TAG, "insert(), values = " + values);

        if (initDataBase())
        {
            return insert2DB(uri, values);
        }

        Log.w(TAG, "insert(), initDataBase failed");
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        Log.d(TAG, "delete(), uri = " + uri);
        //        VLog.d(TAG, "delete(), selection = " + selection);
        //        VLog.d(TAG, "delete(), selectionArgs = " + JsonUtils.toJson(selectionArgs));

        if (initDataBase())
        {
            return deleteFromDB(uri, selection, selectionArgs);
        }

        Log.w(TAG, "delete(), initDataBase failed");
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        Log.d(TAG, "update(), values = " + values);
        //        VLog.d(TAG, "update(), selection = " + selection);
        // VLog.d(TAG, "update(), selectionArgs = " + JsonUtils.toJson(selectionArgs));

        if (initDataBase())
        {
            return update2DB(uri, values, selection, selectionArgs);
        }

        Log.w(TAG, "update2DB(), initDataBase failed");
        return 0;
    }

    /**
     * CRUD 的参数是 Uri，根据 Uri 获取对应的表名
     *
     * @param uri
     * @return
     */
    private String getTableName(final Uri uri)
    {
        String tableName = "";
        int match = mUriMatcher.match(uri);
        switch (match)
        {
            case TABLE_CODE_INFO:
                tableName = TABLE_NAME;
                break;
        }
        //        VLog.d(TAG, "UriMatcher " + uri.toString() + ", result: " + match);
        return tableName;
    }

    private int update2DB(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        String table = getTableName(uri);
        try
        {
            return mDatabase.update(table, values, selection, selectionArgs);
        } catch (Exception e)
        {
            Log.e(TAG, "update2DB(), exception = " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public Uri insert2DB(Uri uri, ContentValues values)
    {
        String table = getTableName(uri);
        try
        {
            long id = mDatabase.insert(table, null, values);
            Log.d(TAG, "insert2DB(), id = " + id);

            // 当该URI的ContentProvider数据发生变化时，通知外界（即访问该ContentProvider数据的访问者）
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (Exception e)
        {
            Log.e(TAG, "insert2DB(), exception = " + e.getMessage());
            e.printStackTrace();
        }

        return uri;
    }

    public Cursor queryFromDB(Uri uri, String[] projection, String selection,
                              String[] selectionArgs, String sortOrder)
    {
        String table = getTableName(uri);
        try
        {
            return mDatabase
                    .query(table, projection, selection, selectionArgs, null, null, sortOrder,
                            null);

        } catch (Exception e)
        {
            Log.e(TAG, "queryFromDB(), exception = " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public int deleteFromDB(Uri uri, String selection, String[] selectionArgs)
    {
        String table = getTableName(uri);
        try
        {
            return mDatabase.delete(table, selection, selectionArgs);
        } catch (Exception e)
        {
            Log.e(TAG, "deleteFromDB(), exception = " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    private SQLiteDatabase mDatabase;

    public boolean initDataBase()
    {
        Context mContext = getContext();

        if (mDatabase == null)
        {
            try
            {
                mDatabase = new DbOpenHelper(mContext).getWritableDatabase();
            } catch (Exception e)
            {
                Log.e(TAG, "initDataBase(), exception = " + e.getMessage());
                e.printStackTrace();
            }
        }

        return mDatabase == null ? false : true;
    }
}
