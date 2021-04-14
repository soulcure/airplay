package swaiotos.channel.iot.http;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.utils.AppUtils;

public class SignUtils {

    private static final String APP_KEY = "KSiVM12wRNu1WNN5";

    public static Map<String, String> signMap(Map<String, String> map) {
        String signStr = sign(map, APP_KEY);
        map.put("sign", signStr);
        return map;
    }


    public static String sign(Map<String, String> map) {
        return sign(map, APP_KEY);
    }


    public static String sign(Map<String, String> map, String appKey) {
        String sortStr = sortToString(map); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        return AppUtils.md5(sortStr + appKey);
    }


    /**
     * 字典排序
     *
     * @param params
     * @return
     */
    public static String sortToString(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            //排除key=sign,key=sign_code,value为空
            if (key.equalsIgnoreCase("sign")
                    || key.equalsIgnoreCase("sign_code")
                    || TextUtils.isEmpty(value)) {
                continue;
            }

            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                sb.append(key).append("=").append(value);
            } else {
                sb.append(key).append("=").append(value).append("&");
            }
        }

        return sb.toString();
    }


}
