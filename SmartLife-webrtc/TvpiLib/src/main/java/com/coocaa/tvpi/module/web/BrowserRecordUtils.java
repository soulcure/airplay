package com.coocaa.tvpi.module.web;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.room.Room;

import com.coocaa.publib.PublibHelper;
import com.coocaa.tvpi.module.io.HomeIOThread;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class BrowserRecordUtils {
    private static List<WebRecordBean> webRecordBeanList;
    static String TAG = "SmartBrowser";

    static WebRecordDb db = null;

    public static List<WebRecordBean> getRecord(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(PublibHelper.getContext(), WebRecordDb.class, "ss_web_record").enableMultiInstanceInvalidation().build();
        }
        if (webRecordBeanList != null) {
            return webRecordBeanList; //每次从缓存读就可以了
        }
        webRecordBeanList = new LinkedList<>();
        if (isUIThread()) {
            HomeIOThread.execute(new Runnable() {
                @Override
                public void run() {
                    initList();
                }
            });
        } else {
            initList();
        }

        return webRecordBeanList;
    }

    public static void addWebRecord(Context context, WebRecordBean webRecordBean) {
        if (db == null) {
            db = Room.databaseBuilder(PublibHelper.getContext(), WebRecordDb.class, "ss_web_record").enableMultiInstanceInvalidation().build();
        }
        if (webRecordBeanList != null && webRecordBean != null) {
            boolean hasRecord = false;
            Iterator<WebRecordBean> iter = webRecordBeanList.iterator();
            while (iter.hasNext()) {
                WebRecordBean bean = iter.next();
                if (TextUtils.equals(bean.getWebUrl(), webRecordBean.getWebUrl())) {
                    hasRecord = true;
                    bean.set(webRecordBean);
                    break;
                }
            }
            if (!hasRecord) {
                webRecordBeanList.add(webRecordBean);
            }
        }
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                db.dao().addRecord(webRecordBean);
            }
        });
        Log.d(TAG, "++ addWebRecord : " + webRecordBean);
    }

    public static void removeWebRecord(Context context, WebRecordBean webRecordBean) {
        if (db == null) {
            db = Room.databaseBuilder(PublibHelper.getContext(), WebRecordDb.class, "ss_web_record").enableMultiInstanceInvalidation().build();
        }
        Log.d(TAG, "-- removeWebRecord : " + webRecordBean);
        if (webRecordBeanList != null && webRecordBean != null) {
            Iterator<WebRecordBean> iter = webRecordBeanList.iterator();
            while (iter.hasNext()) {
                WebRecordBean bean = iter.next();
                if (TextUtils.equals(bean.getWebUrl(), webRecordBean.getWebUrl())) {
                    iter.remove();
                    break;
                }
            }
        }
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                db.dao().deleteRecord(webRecordBean);
            }
        });
    }

    private static void initList() {
        List<WebRecordBean> list = db.dao().getAll();
        Log.d(TAG, "getRecord : " + list);
        if (list != null) {
            for (WebRecordBean bean : list) {
                Log.d(TAG, "load from db : " + bean);
                if (!TextUtils.isEmpty(bean.getWebUrl()) && TextUtils.isEmpty(bean.getImageUrl()) && WebIconUtils.isIconExist(PublibHelper.getContext(), bean.getWebUrl())) {
                    bean.setImageUrl(WebIconUtils.getWebIconUrl(PublibHelper.getContext(), bean.getWebUrl()));
                    Log.d(TAG, "fix imageUrl to : " + bean.getImageUrl() + ", title=" + bean.getTitle());
                }
            }
            webRecordBeanList.addAll(list);
        }
    }

    private static boolean isUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static boolean checkUrlExist(String url) {
        if (webRecordBeanList != null) {
            for (WebRecordBean webRecordBean : webRecordBeanList) {
                if (webRecordBean.getWebUrl().equals(url)) {
                    return true;
                }
            }
        }
        return false;
    }
}
