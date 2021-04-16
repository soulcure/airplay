package com.coocaa.smartscreen.repository.utils;

import android.util.Log;

import com.coocaa.smartscreen.network.common.Constants;
import com.coocaa.smartscreen.network.util.MD5Util;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @ClassName IOTConstants
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/11/1
 * @Version TODO (write something)
 */
public class IOTServerUtil {

    private static final String TAG = IOTServerUtil.class.getCanonicalName();

    private final static String APP_KEY = "81dbba5e74da4fcd8e42fe70f68295a6";
    private final static String SECRET = "50c08407916141aa878e65564321af5f";

    public static HashMap<String, String> getQueryMap(HashMap<String, String> queryMap) {
        HashMap<String, String> pairs = new HashMap<>();
        if (null != queryMap) {
            pairs.putAll(queryMap);
        }
        pairs.put("appkey", APP_KEY);
        pairs.put("time", System.currentTimeMillis() / 1000 + "");
        pairs.put("sign", getSign(pairs));
        return pairs;
    }

    private static String getSign(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> sortMap = sortMapByKey(map);
        Iterator<Map.Entry<String, String>> iterator = sortMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            sb.append(entry.getKey());
            sb.append(entry.getValue());
        }
        sb.append(SECRET);
        String signStr = sb.toString();
        Log.d(TAG, "signStr=" + signStr);
        String sign = MD5Util.getMd5(signStr).toLowerCase();
        Log.d(TAG, "sign=" + sign);
        return sign;
    }

    private static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, String> sortMap = new TreeMap<String, String>(new MapComparator());
        sortMap.putAll(map);
        return sortMap;
    }

    private static class MapComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            return str1.compareTo(str2);
        }
    }

}
