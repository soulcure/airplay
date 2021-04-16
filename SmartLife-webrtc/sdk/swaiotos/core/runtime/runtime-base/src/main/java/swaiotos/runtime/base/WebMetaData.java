package swaiotos.runtime.base;

import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

public class WebMetaData {
    private static LruCache<String, String> titleMap = new LruCache<>(20);
    private static LruCache<String, String> descriptionMap = new LruCache<>(20);

    private final static String TAG = "H5";

    public static void putTitle(String url, String title) {
        Log.d(TAG, "putTitle, url=" + url + ", title=" + title);
        if(TextUtils.isEmpty(url) || TextUtils.isEmpty(title))
            return ;

        titleMap.put(url, title);
    }

    public static String getTitle(String url) {
        if(TextUtils.isEmpty(url))
            return null;
        Log.d(TAG, "getTitle, url=" + url + ", title=" + titleMap.get(url));
        return titleMap.get(url);
    }

    public static void putDescription(String url, String description) {
        Log.d(TAG, "putDescription, url=" + url + ", description=" + description);
        if(TextUtils.isEmpty(url) || TextUtils.isEmpty(description))
            return ;

        descriptionMap.put(url, description);
    }

    public static String getDescription(String url) {
        if(TextUtils.isEmpty(url))
            return null;
        Log.d(TAG, "getDescription, url=" + url + ", des=" + descriptionMap.get(url));
        return descriptionMap.get(url);
    }
}
