package swaiotos.channel.iot.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.List;

public class AppUtils {


    /**
     * 获取manifests中的meta_data值
     *
     * @param context
     * @return
     */
    public static String getMetaData(Context context, String key) {
        String value = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                value = appInfo.metaData.getString(key);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }


    /**
     * md5验证
     *
     * @param file 文件
     * @return md5
     */

    public static String md5(File file) {
        String res;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int len = 0;
            while ((len = fis.read(b)) != -1) {
                md.update(b, 0, len);
            }

            res = md5(md);

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            res = "";
        }

        return res;
    }


    private static String md5(byte[] source) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            StringBuffer result = new StringBuffer();
            for (byte b : md5.digest(source)) {
                result.append(Integer.toHexString((b & 0xf0) >>> 4));
                result.append(Integer.toHexString(b & 0x0f));
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * md5验证
     *
     * @param file 文件
     * @param md5  md5验证码
     * @return
     */

    public static boolean checkFileMd5(File file, String md5) {
        boolean flag = false;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int len = 0;
            while ((len = fis.read(b)) != -1) {
                md.update(b, 0, len);
            }

            if (md5(md).equals(md5)) {
                flag = true;
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return flag;
    }

    /**
     * 获得md5验证码
     *
     * @param md5 值
     * @return 字符串
     */
    public static synchronized String md5(MessageDigest md5) {
        StringBuffer strBuf = new StringBuffer();
        byte[] result16 = md5.digest();
        char[] digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
                'b', 'c', 'd', 'e', 'f'};
        for (int i = 0; i < result16.length; i++) {
            char[] c = new char[2];
            c[0] = digit[result16[i] >>> 4 & 0x0f];
            c[1] = digit[result16[i] & 0x0f];
            strBuf.append(c);
        }

        return strBuf.toString();
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        return getMD5(string.getBytes(Charset.forName("UTF-8")));
    }


    private static String getMD5(byte[] source) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            StringBuilder result = new StringBuilder();
            for (byte b : md5.digest(source)) {
                result.append(Integer.toHexString((b & 0xf0) >>> 4));
                result.append(Integer.toHexString(b & 0x0f));
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static boolean isTopActivity(Context context, String cmdName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTask = manager.getRunningTasks(5);
        String cmpNameTemp = null;
        if (runningTask != null && runningTask.size() > 0) {
            cmpNameTemp = (runningTask.get(0).topActivity).getClassName();
        }
        if (null == cmpNameTemp) return false;
        return cmpNameTemp.equals(cmdName);
    }


    public static String getWifiInfoSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifiInfoSSID = wifiInfo.getSSID();
        return wifiInfoSSID.replace("\"", "").trim();
    }

}
