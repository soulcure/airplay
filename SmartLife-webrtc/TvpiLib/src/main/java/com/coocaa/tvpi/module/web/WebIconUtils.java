package com.coocaa.tvpi.module.web;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.smtt.utils.Md5Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class WebIconUtils {

    private static String TAG = "SmartWebInfo";

    public final static String DIR = "icons";

    private final static Object lock = new Object();

    public static String getWebIconUrl(Context context, String url) {
        if(context == null || TextUtils.isEmpty(url))
            return null;
        String fileName = Md5Utils.getMD5(url);
        return Uri.fromFile(new File(context.getDir(DIR, Context.MODE_PRIVATE), fileName)).toString();
    }

    public static boolean isIconExist(Context context, String url) {
        if(context == null || TextUtils.isEmpty(url))
            return false;
        synchronized (lock) {
            try {
                String fileName = Md5Utils.getMD5(url);
                File file = new File(context.getDir(DIR, Context.MODE_PRIVATE), fileName);
                boolean exist = file.exists() && file.length() > 0;
                Log.d(TAG, "isIconExist url=" + url + ", ret=" + exist);
                return exist;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static void saveIcon(Context context, String url, Bitmap bitmap) {
        if(context == null || TextUtils.isEmpty(url) || bitmap == null)
            return ;
        synchronized (lock) {
            if(isIconExist(context, url)) {
                Log.d(TAG, "icon already exist, no need to save, url=" + url);
                return ;
            }
            try {
                String fileName = Md5Utils.getMD5(url);
                File file = new File(context.getDir(DIR, Context.MODE_PRIVATE), fileName);
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                Log.d(TAG, "saveIcon success, url=" + url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String loadIconAsUrl(Context context, String url) {
        if(context == null || TextUtils.isEmpty(url))
            return null;
        synchronized (lock) {
            try {
                String fileName = Md5Utils.getMD5(url);
                File file = new File(context.getDir(DIR, Context.MODE_PRIVATE), fileName);
                if(file.exists() && file.length() > 0) {
                    String path = file.getAbsolutePath();
                    Log.d(TAG, "loadIconAsUrl success, url=" + url);
                    return path;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Bitmap loadIconAsBitmap(Context context, String url) {
        if(context == null || TextUtils.isEmpty(url))
            return null;
        synchronized (lock) {
            try {
                String fileName = Md5Utils.getMD5(url);
                File file = new File(context.getDir(DIR, Context.MODE_PRIVATE), fileName);
                if(file.exists() && file.length() > 0) {
                    FileInputStream fis = new FileInputStream(file);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    fis.close();
                    Log.d(TAG, "loadIconAsBitmap success, url=" + url);
                    return bitmap;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
