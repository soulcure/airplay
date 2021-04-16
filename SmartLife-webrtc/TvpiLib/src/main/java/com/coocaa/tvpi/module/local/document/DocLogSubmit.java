package com.coocaa.tvpi.module.local.document;

import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;

import java.util.Map;

/**
 * @Description: 文档相关数据采集
 * @Author: wzh
 * @CreateDate: 1/5/21
 */
public class DocLogSubmit {

    //点击快速添加文档按钮
    public final static String EVENTID_CLICK_ADD_DOC_BTN = "click_add_document_btn";
    //选择添加文档方式
    public final static String EVENTID_CLICK_ADD_DOC_TYPE = "click_add_document_type";
    //添加选定的文档
    public final static String EVENTID_ADD_CLICKED_DOC = "add_clicked_document";
    //从其他应用分享文档至智屏
    public final static String EVENTID_OPEN_DOC_BY_OTHER_APP = "open_document_by_other_app";
    //点击首页的文档
    public final static String EVENTID_CLICK_DOC = "click_document";
    //点击投屏按钮
    public final static String EVENTID_CLICK_CAST_DOC_BTN = "click_cast_document_btn";
    //弹出删除确认窗后，点击确定按钮时提交
    public final static String EVENTID_DELETE_DOC = "delete_document";

    public static void submit(String eventId) {
        try {
            submit(eventId, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void submit(String eventId, String key, String value) {
        try {
            submit(eventId, LogParams.newParams().append(key, value).getParams());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void submit(String eventId, Map<String, String> params) {
        try {
            LogSubmit.event(eventId, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
