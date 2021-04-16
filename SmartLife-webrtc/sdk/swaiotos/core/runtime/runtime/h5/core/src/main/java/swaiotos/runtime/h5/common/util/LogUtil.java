package swaiotos.runtime.h5.common.util;

import android.util.Log;


/**
 * 日志打印工具类
 * Created by AwenZeng on 2019/01/03.
 */
public class LogUtil {
    private static String TAG = "CCApplet";
    public static Boolean isDebug = true;


    public static void androidLog(String msg) {
        if (isDebug) {
            Log.d(TAG,msg);
        }
    }

    public static void androidLog(String tag, String msg) {
        if (isDebug) {
            Log.d(tag,msg);
        }
    }

    public static void e(String msg){
        if (isDebug) {
            Log.e(TAG,msg);
        }
    }

    public static void w(String msg){
        if (isDebug) {
            Log.e(TAG,msg);
        }
    }

    public static void d(String msg){
        if (isDebug) {
            Log.e(TAG,msg);
        }
    }

    public static void i(String msg){
        if (isDebug) {
            Log.e(TAG,msg);
        }
    }

    public static void log(String msg, char level) {
        if (isDebug) {
            if ('e' == level) { // 输出错误信息
                Log.e(TAG,msg);
            } else if ('w' == level) {
                Log.w(TAG,msg);
            } else if ('d' == level) {
                Log.d(TAG,msg);
            } else if ('i' == level) {
                Log.i(TAG,msg);
            } else {
                Log.v(TAG,msg);
            }
        }
    }
}
