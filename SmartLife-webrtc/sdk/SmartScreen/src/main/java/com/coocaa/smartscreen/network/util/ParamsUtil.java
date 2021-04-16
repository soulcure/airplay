package com.coocaa.smartscreen.network.util;


import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.network.common.Constants;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.smartscreen.utils.MapSortUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created by WHY on 2017/2/13.
 */
public class ParamsUtil {

    private static final String TAG = ParamsUtil.class.getSimpleName();

    public static String tv_source = "iqiyi";

    public static HashMap<String, Object> getQueryMap(HashMap<String, Object> queryMap){
        HashMap<String, Object> pairs = new HashMap<>();
        if (null != queryMap) {
            pairs.putAll(queryMap);
        }
        pairs.put("appkey", Constants.APP_KEY_TVPAI);
        pairs.put("time", System.currentTimeMillis() / 1000);
        pairs.put("vuid", getUniquePsuedoID());
        pairs.put("version_code", "57");
        if (!pairs.containsKey("tv_source")) {
            pairs.put("tv_source", SSConnectManager.getInstance().getVideoSource());
        }
        try {
            String token = Repository.get(LoginRepository.class).queryToken();
            if (!TextUtils.isEmpty(token)) {
                pairs.put("token", token);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getQueryMap: 没有登录信息");
        }

        Collection<String> keyset= pairs.keySet();
        List<String> list = new ArrayList<String>(keyset);

        //对key键值按字典升序排序
        Collections.sort(list);

        StringBuilder sortedPairStr = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            String key = list.get(i);
            Object value = pairs.get(key);
            sortedPairStr.append(key + value);
        }
        sortedPairStr.append(Constants.APP_SALT_TVPAI);
        Log.d(TAG,"sortUrlParameters after sort===== : " + sortedPairStr.toString());
        String signStr = sortedPairStr.toString();
        String sign = MD5Util.getMD5String(signStr).toLowerCase(Locale.getDefault());
        pairs.put("sign", sign);
        return pairs;
    }

    public static String getUniquePsuedoID() {
        String serial = "serial";

        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13 位

        try {
            //API>=9 使用serial号
            serial = Build.class.getField("SERIAL").get(null).toString();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }


    public static String getSignByQueryAndBodyParams(Map<String,String> queryAndBodyParamsMap){

        Log.w(TAG,"传入的原始query和body参数：" + queryAndBodyParamsMap.toString());
        String sign = "";
        //按字母顺序拼接参数
        List<Map.Entry<String, String>> list = MapSortUtil.sortMap(queryAndBodyParamsMap);
        for (int i = 0; i < list.size(); i++) {
            if(i < list.size() -1) {
                sign += (list.get(i).getKey() + "=" + list.get(i).getValue() + "&");
            }else {
                sign += (list.get(i).getKey() + "=" + list.get(i).getValue() );
            }
        }
        sign += Constants.VIDEO_CALL_SIGN_SECRET;//加盐
        Log.w(TAG,"重排序拼接后的加盐参数：" + sign);
        String md5String = MD5Util.getMD5String(sign);
        Log.w(TAG,"最终获取到的md5参数：" + md5String);
        return md5String;
    }

    public static JSONArray getStringJsonArray(List<String> list) {

        JSONArray jsonArray = new JSONArray();
        if (list!=null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                String parameter = list.get(i);
                if (parameter != null) {
                    jsonArray.put(parameter);
                } else {
//                    jsonArray.put("");
                }
                Log.d(TAG, "label=" + list.get(i));

            }
        } else {
//            jsonArray.put("");
        }
//        if (jsonArray.length() > 0)
        String str = jsonArray.toString();
        Log.d(TAG, "str==" + str);
        return jsonArray;
    }

    public static String getJsonStringParams(Map<String, Object> params) {
        try {
            JSONObject json = new JSONObject();
            for (String key : params.keySet()) {
                System.out.println("key= "+ key + " and value= " + params.get(key));
                json.put(key, params.get(key));
            }
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /*coocaa passport start*/

    /**
     * 该加密区别于以前的方法
     * clientId  改为 client_id
     */
    public static HashMap<String, String> getCoocaaAccountPublicMap(HashMap<String, String> queryMap, HashMap<String, String> fieldMap) {
        HashMap<String, String> map = new HashMap<>();
        if (null != queryMap) {
            map.putAll(queryMap);
        }
        if (null != fieldMap) {
            map.putAll(fieldMap);
        }
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        map.put("time", time);
        map.put("client_id", Constants.getCoocaaClientId());
        map.put("sign", getCoocaaAccountSignString(queryMap, map));
        return map;
    }

    private static String getCoocaaAccountSignString(HashMap<String, String> queryMap, HashMap<String, String> fieldMap) {
        String sign = sortCoocaaAccountUrlParameters(queryMap,fieldMap);
        return CipherUtil.md5(sign);
    }

    private static String sortCoocaaAccountUrlParameters(HashMap<String, String> queryMap, HashMap<String, String> fieldMap){
        HashMap<String, String> pairs = new HashMap<>();
        if (null != queryMap) {
            pairs.putAll(queryMap);
        }
        if (null != fieldMap) {
            pairs.putAll(fieldMap);
        }
        Collection<String> keyset= pairs.keySet();
        List<String> list = new ArrayList<String>(keyset);

        //对key键值按字典升序排序
        Collections.sort(list);

        StringBuilder sortedPairStr = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                sortedPairStr.append(list.get(i) + "=" + pairs.get(list.get(i)));
            } else {
                sortedPairStr.append("&" + list.get(i) + "=" + pairs.get(list.get(i)));
            }
        }
        sortedPairStr.append(Constants.getCoocaaSecret());

//        Log.d(TAG,"sortUrlParameters,pairs:"+sortedPairStr);
        Log.d(TAG,"sortUrlParameters after sort===== : " + sortedPairStr.toString());
        return sortedPairStr.toString();
    }

    /*coocaa passport end*/

    public static JSONArray getIntegerJsonArray(List<Integer> list) {

        JSONArray jsonArray = new JSONArray();
        if (list!=null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Integer parameter = list.get(i);
                if (parameter != null) {
                    jsonArray.put(parameter);
                } else {
//                    jsonArray.put("");
                }
//                IRLog.d(TAG, "label=" + list.get(i));

            }
        } else {
//            jsonArray.put("");
        }
//        if (jsonArray.length() > 0)
        String str = jsonArray.toString();
//        IRLog.d(TAG, "str==" + str);
        return jsonArray;
    }

    public static Map<String,Object> getCoocaaAccountPublicQueryUrl() {
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        String qs = "client_id=" + Constants.getCoocaaClientId() + "&time=" + time;
        String plain = qs + Constants.getCoocaaSecret();
        String sign = CipherUtil.md5(plain);
        Map<String,Object> map = new HashMap<>();
        map.put("client_id",Constants.getCoocaaClientId());
        map.put("time",time);
        map.put("sign",sign);
        return map;
    }

    public static Map<String,Object> getCoocaaAccountPublicQueryUrl(Map<String,Object> queryMap) {
        Map<String,Object> map = getCoocaaAccountPublicQueryUrl();
        if (null != queryMap) {
            map.putAll(queryMap);
        }
        return map;
    }
}
