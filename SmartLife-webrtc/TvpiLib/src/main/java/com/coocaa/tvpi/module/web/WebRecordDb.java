package com.coocaa.tvpi.module.web;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {WebRecordBean.class}, version = 1, exportSchema = false)
public abstract class WebRecordDb extends RoomDatabase {
    public abstract WebRecordDao dao();
}
