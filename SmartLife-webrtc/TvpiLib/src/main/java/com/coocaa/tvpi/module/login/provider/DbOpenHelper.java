package com.coocaa.tvpi.module.login.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Gwen on 2018/6/4.
 */

public class DbOpenHelper extends SQLiteOpenHelper
{

    private final static String DB_NAME = "user_info.db";
    public final static String TABLE_NAME = "account";
    private final static int DB_VERSION = 1;
    private final String SQL_CREATE_TABLE = "create table if not exists " + TABLE_NAME +
            "(_id integer primary key, type TEXT, info BLOB)";

    public DbOpenHelper(final Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db)
    {
        try
        {
            db.execSQL(SQL_CREATE_TABLE);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
    {

    }
}