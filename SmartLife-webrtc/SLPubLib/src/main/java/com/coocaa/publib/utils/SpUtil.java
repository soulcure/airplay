package com.coocaa.publib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by WHY on 2017/7/21.
 */

public class SpUtil {

    // 不同的返回数据的key定义
    public class Keys {
        public static final String DEVICE_ACCESS_TOKEN = "DEVICE_ACCESS_TOKEN";
        public static final String ZPLSID = "ZPLSID";


        public final static String XIAOWEI_AI_APP_STATUS_RESULT = "XIAOWEI_AI_APP_STATUS_RESULT_V1";
        public final static String SCREEN_SHOT_APP_STATUS_RESULT = "SCREEN_SHOT_APP_STATUS_RESULT";
        public final static String IS_SUPPORT_LIVE_PUSH_DE = "IS_SUPPORT_LIVE_PUSH_DE";

        public static final String PKG_NAME_XIAOWEI_AI = "com.skyworth.lafite.srtnj.speechserver";
        public static final String PKG_NAME_GZ = "com.gzads.tvac";
        public static final String PKG_NAME_KU_YUN_COOCAA = "com.kuyun.smarttv.coocaatv";
        public static final String PKG_NAME_KU_YUN_SKYWORTH = "com.kuyun.skyworth.smarttv";
        public static final String PKG_NAME_KU_YUN_IDENTIFER = "com.kuyun.skyworth.identifer";
        // 遥控器震动
        public static final String REMOTE_VIBRATE = "vibrate";
        // 截图水印
        public static final String SCREENSHOT_WATERMARK = "SCREENSHOT_WATERMARK";
        //第一次截图成功
        public static final String FIRST_SCREEN_SHOT = "FIRST_SCREEN_SHOT";
        //云端连接的设备激活id存储
        public static final String WEB_REMOTE_DEVICE = "WEB_REMOTE_DEVICE";
        public static final String WEB_REMOTE_HISTORY_DEVICE = "WEB_REMOTE_HISTORY_DEVICE";
        //第一次安装运行
        public static final String FIRST_START_APP = "FIRST_START_APP";
        //主页上方tab栏
        public static final String HOME_TOP_TAB = "HOME_TOP_TAB";
        //记录提示打开push通知的时间
        public static final String DATE_NOTIFICATIONS_ENABLED = "DATE_NOTIFICATIONS_ENABLED";
        //记录直播收藏的台标
        public static final String TVLIVE_COLLECT_PROGRAMS = "TVLIVE_COLLECT_PROGRAMS";
        //记录直播收藏的台标
        public static final String USER_INFO_RESP = "USER_INFO_RESP";
        //记录直播收藏的台标
        public static final String LOGIN_INFO_RESP = "LOGIN_INFO_RESP";
        //电池优化标志位
        public static final String BATTERY_OPTIMIZE_CLOSE_BY_USER = "BATTERY_OPTIMIZE_CLOSE_BY_USER";
    }

    private static SharedPreferences sp;

    private static SharedPreferences getSp(Context context) {
        if (sp == null) {
            sp = context.getSharedPreferences("SpUtil", Context.MODE_PRIVATE);
        }
        return sp;
    }


    /**
     * 存入字符串
     *
     * @param context 上下文
     * @param key     字符串的键
     * @param value   字符串的值
     */
    public static void putString(Context context, String key, String value) {
        SharedPreferences preferences = getSp(context);
        //存入数据
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 获取字符串
     *
     * @param context 上下文
     * @param key     字符串的键
     * @return 得到的字符串
     */
    public static String getString(Context context, String key) {
        SharedPreferences preferences = getSp(context);
        return preferences.getString(key, "");
    }

    /**
     * 获取字符串
     *
     * @param context 上下文
     * @param key     字符串的键
     * @param value   字符串的默认值
     * @return 得到的字符串
     */
    public static String getString(Context context, String key, String value) {
        SharedPreferences preferences = getSp(context);
        return preferences.getString(key, value);
    }

    /**
     * 保存布尔值
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sp     = getSp(context);
        Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * 获取布尔值
     *
     * @param context  上下文
     * @param key      键
     * @param defValue 默认值
     * @return 返回保存的值
     */
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences sp = getSp(context);
        return sp.getBoolean(key, defValue);
    }

    /**
     * 保存long值
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    public static void putLong(Context context, String key, long value) {
        SharedPreferences sp     = getSp(context);
        Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * 获取long值
     *
     * @param context  上下文
     * @param key      键
     * @param defValue 默认值
     * @return 保存的值
     */
    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences sp = getSp(context);
        return sp.getLong(key, defValue);
    }

    /**
     * 保存int值
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp     = getSp(context);
        Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * 获取long值
     *
     * @param context  上下文
     * @param key      键
     * @param defValue 默认值
     * @return 保存的值
     */
    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = getSp(context);
        return sp.getInt(key, defValue);
    }

    /**
     * 保存对象
     *
     * @param context 上下文
     * @param key     键
     * @param obj     要保存的对象（Serializable的子类）
     * @param <T>     泛型定义
     */
    public static <T extends Serializable> void putObject(Context context, String key, T obj) {
        try {
            put(context, key, obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取对象
     *
     * @param context 上下文
     * @param key     键
     * @param <T>     指定泛型
     * @return 泛型对象
     */
    public static <T extends Serializable> T getObject(Context context, String key) {
        try {
            return (T) get(context, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 存储List集合
     * @param context 上下文
     * @param key 存储的键
     * @param list 存储的集合
     */
    public static void putList(Context context, String key, List<? extends Serializable> list) {
        try {
            put(context, key, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取List集合
     * @param context 上下文
     * @param key 键
     * @param <E> 指定泛型
     * @return List集合
     */
    public static <E extends Serializable> List<E> getList(Context context, String key) {
        try {
            return (List<E>) get(context, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 存储Map集合
     * @param context 上下文
     * @param key 键
     * @param map 存储的集合
     * @param <K> 指定Map的键
     * @param <V> 指定Map的值
     */
    public static <K extends Serializable, V extends Serializable> void putMap(Context context,
                                                                               String key, Map<K, V> map)
    {
        try {
            put(context, key, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <K extends Serializable, V extends Serializable> Map<K, V> getMap(Context context,
                                                                                    String key)
    {
        try {
            return (Map<K, V>) get(context, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

   /* public static <K, V> void putMap(Context context, String key, Map<K, V> map)
    {
        try {
            put(context, key, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <K, V> Map<K, V> getMap(Context context, String key)
    {
        try {
            return (Map<K, V>) get(context, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    /**存储对象*/
    private static void put(Context context, String key, Object obj)
            throws IOException
    {
        if (obj == null) {//判断对象是否为空
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos  = null;
        oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        // 将对象放到OutputStream中
        // 将对象转换成byte数组，并将其进行base64编码
        String objectStr = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
        baos.close();
        oos.close();

        putString(context, key, objectStr);
    }

    /**获取对象*/
    private static Object get(Context context, String key)
            throws IOException, ClassNotFoundException
    {
        String wordBase64 = getString(context, key);
        // 将base64格式字符串还原成byte数组
        if (TextUtils.isEmpty(wordBase64)) { //不可少，否则在下面会报java.io.StreamCorruptedException
            return null;
        }
        byte[]               objBytes = Base64.decode(wordBase64.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream bais     = new ByteArrayInputStream(objBytes);
        ObjectInputStream ois      = new ObjectInputStream(bais);
        // 将byte数组转换成product对象
        Object obj = ois.readObject();
        bais.close();
        ois.close();
        return obj;
    }
}