package com.coocaa.tvpi.module.logcat;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Describe:文件辅助工具类
 * Created by AwenZeng on 2021/03/04
 */
public class FileUtil {

    /**
     * 删除文件
     * @param filePath
     * @return
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }


    /**
     * 获取文件保存路径
     * @param context
     * @return
     */
    public static String getFilePath(Context context){
        String filePath;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            filePath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "interprenter";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            filePath = context.getFilesDir().getAbsolutePath()
                    + File.separator + "interprenter";
        }
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
            Log.i(LogcatHelper.TAG, "创建文件夹");
        }
        Log.i(LogcatHelper.TAG, filePath);
        return filePath;
    }

    /**
     * 文件名生成
     * @return
     */
    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日_HHmmss");
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;// 2012年10月03日 23:41:31
    }
}
