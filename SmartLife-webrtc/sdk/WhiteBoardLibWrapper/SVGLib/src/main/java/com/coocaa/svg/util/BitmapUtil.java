package com.coocaa.svg.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapUtil {

    private final static String TAG = "Save";
    private static final String savePath = Environment.getExternalStorageDirectory().getPath() + "/tvpi" + "/whiteboard/";

    private static boolean createDirectory(final String path) {
        boolean result = false;

        try {
            File file = new File(path);
            boolean isDir = false;
            if (!file.isDirectory()) {
                file.delete();
                isDir = file.mkdirs();
            } else {
                isDir = true;
            }

            if (isDir) {
                Runtime.getRuntime().exec("chmod 777 " + path);
            }

            result = isDir;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 保存图片到指定路径
     *
     * @param context
     * @param *bitmap  要保存的图片
     * @param filePrefix 自定义图片名称
     */
    public static boolean saveImageToGallery(Context context, Bitmap bitmap, String filePrefix) {
        if (!createDirectory(savePath)) {
            Log.e(TAG, "create directory failed!");
            return false;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = filePrefix + simpleDateFormat.format(new Date()) + ".jpg";
        Log.e(TAG, "fileName = "+fileName);
        try {
            File dirFile = new File(savePath);
            //如果不存在，那就建立这个文件夹
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File file = new File(savePath, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            // 其次把文件插入到系统图库
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            // 最后通知图库更新
            String dstFilename = savePath + fileName;
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file" +
                    "://" + dstFilename)));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "save error = "+e.toString());
        }

        return true;
    }
}
