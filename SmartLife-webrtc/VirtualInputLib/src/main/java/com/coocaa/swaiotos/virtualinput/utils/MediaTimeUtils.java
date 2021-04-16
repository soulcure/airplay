package com.coocaa.swaiotos.virtualinput.utils;

import android.util.Log;

import java.util.Formatter;
import java.util.Locale;

/**
 * @ClassName: Utils
 * @Author: AwenZeng
 * @CreateDate: 2020/10/17 17:13
 * @Description: 媒体时间转换工具类
 */
public class MediaTimeUtils {
    private static StringBuilder mFormatBuilder;
    private static Formatter mFormatter;

    static {
        // 转换成字符串的时间
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    /**
     * 把毫秒转换成：1:20:30这里形式
     *
     * @param timeMs
     * @return
     */
    public static String stringForTime(long timeMs) {
//        if ((timeMs % 10L) >= 5L) {
//            timeMs = timeMs + 10L;
//        }
//        if ((timeMs % 100L) >= 50L) {
//            timeMs = timeMs + 100L;
//        }
        long totalSeconds = timeMs / 1000L;
        if ((timeMs % 1000L) >= 500L) {
            totalSeconds++;
        }
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds)
                    .toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 是否是网络资源
     *
     * @param url
     * @return
     */
    public boolean isNetUrl(String url) {
        boolean result = false;
        if (url != null) {

            if (url.toLowerCase().startsWith("http")
                    || url.toLowerCase().startsWith("rtsp")
                    || url.toLowerCase().startsWith("mms")) {
                result = true;
            }
        }
        return result;
    }
}