package com.coocaa.tvpi.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName ClipboardUtil
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/10/21
 * @Version TODO (write something)
 */
public class ClipboardUtil {
    public static final String HTTP = "http://";

    private static final String TAG = ClipboardUtil.class.getSimpleName();

    public static final String OFFICIAL_WEBSITE = "http://tvpi.coocaa.com/swaiot/index.html";
    public static final String KEY_WORD = "COOC@@";

    //系统剪贴板-复制:   s为内容
    public static void copy(Context context, String s) {
        if (context == null) {
            return;
        }
        // 获取系统剪贴板
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
        ClipData clipData = ClipData.newPlainText(null, s);
        // 把数据集设置（复制）到剪贴板
        clipboard.setPrimaryClip(clipData);
    }

    //系统剪贴板-获取:
    public static String getCopy(Context context) {
        if (context == null) {
            return "";
        }
        // 获取系统剪贴板
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 返回数据
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            // 从数据集中获取（粘贴）第一条文本数据
            CharSequence charSequence = clipData.getItemAt(0).getText();
            if (charSequence != null) {
                return charSequence.toString();
            }
        }
        return null;
    }

    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> getURLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();

        try {
            Uri uri = Uri.parse(URL);
            Set<String> keySet = uri.getQueryParameterNames();
            for (String s : keySet) {
                mapRequest.put(s, uri.getQueryParameter(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapRequest;
    }

    public static boolean isAvaliableTime(long timestamp) {
        boolean isAvaliable = false;
        long time = System.currentTimeMillis() / 1000;
        long diff = time - timestamp;
        long min = diff / (60);
        Log.d(TAG, "is over min: " + min);
        if (min < 30) {
            isAvaliable = true;
        }
        return isAvaliable;
    }


    /**
     * 补充
     *
     * @param keyword
     * @return
     */
    public static String conversionKeywordLoadOrSearch(String keyword) {
        keyword = keyword.trim();

        if (!keyword.startsWith("http")) {
            keyword = HTTP + keyword;
        }
        return keyword;
    }
}
