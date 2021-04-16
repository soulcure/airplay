package com.coocaa.tvpi.module.local.document;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/10/23
 */
public class DocumentUtil {

    private final static String TAG = "DocumentUtil";
    //临时路径
    public final static String READER_TEMP_PATH = "/storage/emulated/0/TbsReaderTemp";
    public final static String PATH_EXTERNAL = Environment.getExternalStorageDirectory().getPath();
    public final static String SAVE_DOC_PATH = PATH_EXTERNAL + "/CCDoc/";
    public final static String SAVE_DOC_FILE_DIR = "files/";
    public final static String SAVE_DOC_VIDEO_DIR = "video/";

    public final static String KEY_FILE_PATH = "file_path";
    public final static String KEY_FILE_SIZE = "file_size";
    public final static String KEY_SOURCE_APP = "source_app";
    public final static String KEY_SOURCE_PAGE = "source_page";
    public final static String KEY_SCAN_SOURCE = "scan_source";//扫描来源
    public final static String SP_KEY_RECORD = "doc_play_record";//文档浏览记录
    public final static String SP_KEY_SCAN_PATH = "doc_scan_path";//用户自己选择的文档路径
    public final static String SOURCE_PAGE_OTHER_APP = "other_app";//第三方APP
    public final static String SOURCE_PAGE_DOC_MAIN = "doc_main";//文档主页
    public final static String SOURCE_PAGE_DOC_SCAN = "doc_scan";//文档扫描页面

    /**
     * 根据文件路径获取文件名和后缀
     *
     * @param filePath
     * @return
     */
    public static String getFileNameFromPath(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        String fileName = "";
        int start = filePath.lastIndexOf("/");
        if (start != -1) {
            fileName = filePath.substring(start + 1);
        }
        Log.i(TAG, "getFileNameFromPath: " + fileName);
        return fileName;
    }

    /***
     * 获取文件类型
     *
     * @param filePath
     * @return
     */
    public static String getFileType(String filePath) {
        String str = "";
        if (TextUtils.isEmpty(filePath)) {
            Log.d(TAG, "filePath---->null");
            return str;
        }
        Log.d(TAG, "getFileType filePath:" + filePath);
        int i = filePath.lastIndexOf('.');
        if (i <= -1) {
            Log.d(TAG, "i <= -1");
            return str;
        }
        str = filePath.substring(i + 1);
        Log.d(TAG, "getFileType------>" + str);
        return str;
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 根据目录删除文件
     *
     * @param file 文件目录
     */
    public static void deleteFile(File file) {
        try {
            //flie：要删除的文件夹的所在位置
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    deleteFile(f);
                }
//                file.delete();//如要保留文件夹，只删除文件，请注释这行
            } else if (file.exists()) {
                Log.i(TAG, "deleteFile: " + file.getAbsolutePath());
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 转换文件大小
     *
     * @param bytes
     * @return
     */
    public static String formetFileSize(long bytes) {
        int u = 0;
        for (; bytes > 1024 * 1024; bytes >>= 10) {
            u++;
        }
        if (bytes > 1024)
            u++;
        return String.format("%.2f %cB", bytes / 1024f, " kMGTPE".charAt(u));
    }

    public static final String byteToString(long size) {
        long GB = 1024 * 1024 * 1024;//定义GB的计算常量
        long MB = 1024 * 1024;//定义MB的计算常量
        long KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + " GB   ";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + " MB   ";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + " KB   ";
        } else {
            resultSize = size + " B   ";
        }
        return resultSize;
    }

    /**
     * 正则表达式匹配字符串中的数字（包括小数点）
     *
     * @param str
     * @return
     */
    public static double matchNumber(String str) {
        try {
//            String regex="([1-9]\\d*\\.?\\d+)|(0\\.\\d*[1-9])|(\\d+)";
            String regex = "(\\d+(\\.\\d+)?)";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(str);
            if (m.find()) {
                String ret = m.group();
                Log.i("DocumentUtil", "matchNumber: " + str + "-->" + ret);
                return Double.parseDouble(ret);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * 正则表达式匹配字符串中的字符（除数字外，包括小数点的数字）
     *
     * @param str
     * @return
     */
    public static String matchNonNumber(String str) {
        try {
//            String regex="([1-9]\\d*\\.?\\d+)|(0\\.\\d*[1-9])|(\\d+)";
            String regex = "(\\d+(\\.\\d+)?)";
            Pattern r = Pattern.compile(regex);
            Matcher m = r.matcher(str);
            if (m.find()) {
                String ret = str.replace(m.group(), "");
                Log.i("DocumentUtil", "matchNonNumber: " + str + "-->" + ret);
                return ret.trim();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 是否是安卓11
     *
     * @return
     */
    public static boolean isAndroidR() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

}
