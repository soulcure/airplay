package com.coocaa.tvpi.module.local.document;

import android.content.Context;
import android.util.Log;

import com.coocaa.publib.data.local.DocumentData;
import com.coocaa.smartscreen.utils.SpUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Description: 文档数据接口
 * @Author: wzh
 * @CreateDate: 4/7/21
 */
public class DocumentDataApi {

    private final static String TAG = "DocumentDataApi";

    /**
     * 获取已添加的文档记录列表（已按时间排序）
     *
     * @param c
     * @return
     */
    public static List<DocumentData> getRecordList(Context c) {
        List<DocumentData> resultList = new ArrayList<>();
        List<DocumentData> list = SpUtil.getList(c, DocumentUtil.SP_KEY_RECORD);
        if (list != null && list.size() > 0) {
            for (DocumentData data : list) {
                File file = new File(data.url);
                if (file.exists()) {
                    resultList.add(data);
                } else {
                    Log.i(TAG, "loadData file is not exists: " + data.url);
                }
            }
            Log.d(TAG, "doc record size: " + resultList.size());
            if (resultList.size() > 0) {
                Collections.sort(resultList, new Comparator<DocumentData>() {
                    @Override
                    public int compare(DocumentData o1, DocumentData o2) {
                        return Long.compare(o2.lastModifiedTime, o1.lastModifiedTime);
                    }
                });
            }
        }
        return resultList;
    }
}
