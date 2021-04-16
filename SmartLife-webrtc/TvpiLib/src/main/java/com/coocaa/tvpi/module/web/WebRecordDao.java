package com.coocaa.tvpi.module.web;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WebRecordDao {

    @Query("select * from web_record")
    List<WebRecordBean> getAll();

    @Query("select * from web_record where webUrl=:url limit 1")
    WebRecordBean getRecordByUrl(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addRecord(WebRecordBean bean);

    @Update
    void updateRecord(WebRecordBean bean);

    @Delete
    void deleteRecord(WebRecordBean bean);

    @Query("delete from web_record")
    void deleteAll();
}
