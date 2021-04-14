package swaiotos.channel.iot.utils;

import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppUtils {

    private AppUtils() {
        throw new AssertionError();
    }

    /**
     * 获取当前应用的版本号
     *
     * @param context 场景
     * @return String
     */
    public static String getVersion(Context context) {
        String packageName = context.getPackageName();

        return getAppVersion(context, packageName);

    }


    /**
     * 获取当前应用的版本号
     *
     * @param context 场景
     * @return int
     */
    public static int getVerCode(Context context) {
        String packageName = context.getPackageName();

        return getAppVerCode(context, packageName);

    }

    /**
     * 获取指定包名的应用版本号
     *
     * @param context     场景
     * @param packageName 包名
     * @return String
     */
    private static String getAppVersion(Context context, String packageName) {
        String ver = null;

        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            ver = pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ver;
    }

    /**
     * 获取指定包名的应用版本号
     *
     * @param context     场景
     * @param packageName 包名
     * @return String
     */
    private static int getAppVerCode(Context context, String packageName) {
        int ver = 0;

        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            ver = pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ver;
    }


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
     * 安装APK
     */
    public static void installApk(Context context, String apkFilePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkFilePath), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }


    /**
     * 判断应该是否安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (PackageInfo item : pinfo) {
                if (item.packageName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 获取SharedPreferences
     *
     * @param context 场景
     * @return
     */
    public static SharedPreferences getAppSharedPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sp;
    }

    /**
     * 写入SharedPreferences数据
     *
     * @param context 场景
     * @param key     键
     * @param value   值
     */
    public static void setStringSharedPreferences(Context context, String key,
                                                  String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 读取SharedPreferences数据
     *
     * @return String value
     */
    public static String getStringSharedPreferences(Context context,
                                                    String key, String defaultValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPref.getString(key, defaultValue);
    }

    /**
     * 写入SharedPreferences数据
     *
     * @param context 场景
     * @param key     键
     * @param value   值
     */
    public static void setBooleanSharedPreferences(Context context, String key,
                                                   boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * 读取SharedPreferences数据
     *
     * @return boolean value
     */
    public static boolean getBooleanSharedPreferences(Context context,
                                                      String key, boolean defaultValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, defaultValue);
    }

    /**
     * 写入SharedPreferences数据
     *
     * @param context 场景
     * @param key     键
     * @param value   值
     */
    public static void setIntSharedPreferences(Context context, String key,
                                               int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 读取SharedPreferences数据
     *
     * @return int value
     */
    public static int getIntSharedPreferences(Context context, String key,
                                              int defaultValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPref.getInt(key, defaultValue);
    }

    /**
     * 写入SharedPreferences数据
     *
     * @param context 场景
     * @param key     键
     * @param value   值
     */
    public static void setLongSharedPreferences(Context context, String key,
                                                long value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * 读取SharedPreferences数据
     *
     * @return long value
     */
    public static long getLongSharedPreferences(Context context, String key,
                                                long defaultValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                AppConfig.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPref.getLong(key, defaultValue);
    }

    /**
     * 网络是否连通
     */
    public static boolean isNetworkConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo nInfo = cm.getActiveNetworkInfo();
                if (nInfo != null && nInfo.isConnected()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * sim卡是否可读
     */
    public static boolean isCanUseSim(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            return TelephonyManager.SIM_STATE_READY == tm.getSimState();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;
    }


    /**
     * 是否是wifi连接
     *
     * @param context 场景
     */
    public static boolean isWifi(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo nInfo = cm.getActiveNetworkInfo();
                if (nInfo != null) {
                    return nInfo.getTypeName().toUpperCase(Locale.US)
                            .equals("WIFI");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 获取网络类型 网络类型.0:未知;1:wifi; 2:2G; 3:3G  4:4G
     *
     * @param context
     * @return
     */
    public static int getNetworkType(Context context) {
        int strNetworkType = 0;
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // 获取代表联网状态的NetWorkInfo对象
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = 1;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();
                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType = 2;
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = 3;
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = 4;
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA")
                                || _strSubTypeName.equalsIgnoreCase("WCDMA")
                                || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType = 3;
                        }

                        break;
                }
            }
        }

        return strNetworkType;
    }


    /**
     * 获取SIM卡类型  0:未知; 1:移动; 2:联通; 3:电信
     */
    public static int checkSIMCard(Context context) {
        int res = 0;
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telManager.getSimOperator();
        if (operator != null) {
            if (operator.equals("46000") || operator.equals("46002")) {
                res = 1;//中国移动
            } else if (operator.equals("46001")) {
                res = 2;//中国联通
            } else if (operator.equals("46003")) {
                res = 3;//中国电信
            }
        }
        return res;
    }

    /**
     * bytes to hx_kb
     *
     * @param bytes 比特数
     * @return 字符串
     */
    public static String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal kilobyte = new BigDecimal(1024);
        float returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP).floatValue();
        return (returnValue + "  KB ");
    }


    /**
     * bytes to mb
     *
     * @param bytes 比特数
     * @return 字符串
     */
    public static String bytes2mb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP).floatValue();
        return (returnValue + "MB");
    }


    /**
     * @return null may be returned if the specified process not found
     */
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }


    /**
     * 判断系统指定服务是否在运行
     *
     * @param mContext
     * @param serviceClassName
     * @return
     */
    public static boolean isServiceRunning(Context mContext, String serviceClassName) {
        final ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断是否存在同名服务在运行
     *
     * @param context
     * @param serviceClassName
     * @return
     */
    public static boolean isMultiService(Context context, String serviceClassName) {
        boolean res = false;
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            String cls = runningServiceInfo.service.getClassName();
            String spn = runningServiceInfo.service.getPackageName();
            if (cls.endsWith(serviceClassName) && !context.getPackageName().equals(spn)) {
                res = true;
            }
        }
        return res;
    }


    /**
     * 服务停止自己
     *
     * @param context
     * @param serviceClassName
     * @return
     */
    public static boolean stopService(Context context, String serviceClassName) {
        boolean res = false;
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {

            String cls = runningServiceInfo.service.getClassName();
            String spn = runningServiceInfo.service.getPackageName();

            if (cls.endsWith(serviceClassName) && context.getPackageName().equals(spn)) {
                context.stopService(new Intent().setComponent(runningServiceInfo.service));
                res = true;
            }
        }
        return res;
    }

    /**
     * 是否有外置sdcard
     *
     * @return
     */
    public static boolean isExtSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡的路径
     */
    public static String getSdcardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }


    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    /**
     * 判断包是否安装
     *
     * @param context
     * @param packageName：应用包名
     * @return
     */
    public boolean isInstalled(Context context, String packageName) {
        final PackageManager pm = context.getPackageManager();
        try {
            pm.getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    /**
     * 获取外置SD卡路径
     *
     * @return 应该就一条记录或空
     */
    public static String getExtSDCardPath() {
        String result = null;
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("sdcard1")) {
                    String[] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory()) {
                        result = path;
                    }
                }
            }
            isr.close();
        } catch (Exception e) {
        }
        return result;
    }


    /**
     * 本地sdcard上是否有视频资源
     *
     * @param netPath 资源路径
     * @return
     */
    public static String parseLocalPath(String netPath) {
        String result = null;

        String[] strs = netPath.split("serie");
        if (strs != null && strs.length == 2) {
            String ext_sdcard = getExtSDCardPath();
            if (!TextUtils.isEmpty(ext_sdcard)) {
                String video = ext_sdcard + "/serie" + strs[1];
                File file = new File(video);
                if (file.isFile()) {
                    result = video;
                }
            }
        }
        return result;
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

    /**
     * 自适应式从图片获取Drawable，类似于把图片放入drawable目录一样
     *
     * @param context
     * @param pngFile
     * @param density
     * @return
     */
    public static Drawable getDrawableFromFile(Context context, File pngFile,
                                               int density) {
        Bitmap bmp = BitmapFactory.decodeFile(pngFile.getPath());
        if (bmp != null)
            bmp.setDensity(density);

        return new BitmapDrawable(context.getResources(), bmp);
    }

    /**
     * 获取drawable
     */
    public static Drawable getDrawableFromFile(File pngFile) {

        return Drawable.createFromPath(pngFile.getPath());
    }

    /**
     * 获取drawable
     */
    public static Drawable getDrawableFromFile(String filePath) {

        return Drawable.createFromPath(filePath);
    }

    /**
     * 从Assets中读取图片
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is).copy(
                    Bitmap.Config.ARGB_8888, true);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    /**
     * 指定编码获取properties文件中的属性值（解决中文乱码问题）
     *
     * @param properties java.util.Properties
     * @param key        属性key
     * @return
     */
    public static String getProperty(Properties properties, String key, String encoding)
            throws UnsupportedEncodingException {
        //param check
        if (properties == null)
            return null;

        //如果此时value是中文，则应该是乱码
        String value = properties.getProperty(key);
        if (value == null)
            return null;

        //编码转换，从ISO8859-1转向指定编码
        value = new String(value.getBytes("ISO8859-1"), encoding);
        return value;
    }


    /**
     * 是否是手机号码
     *
     * @param num
     * @return
     */
    public static boolean isMobileNum(String num) {
        return Pattern.compile("^1[34578]\\d{9}$").matcher(num).matches();
    }

    /**
     * 是否是短信验证码
     *
     * @param num
     * @return
     */
    public static boolean isSmsCode(String num) {
        return Pattern.compile("\\d{6}").matcher(num).matches();
    }

    /**
     * 是否是IP V4地址
     *
     * @param ipAddress
     * @return
     */
    public static boolean isIpv4(String ipAddress) {


        String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }


    /**
     * 是否是数字
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {

        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static boolean isNumeric(String str) {

        String patternStr = "^[+-]?([0-9]*\\.?[0-9]+|[0-9]+\\.?[0-9]*)([eE][+-]?[0-9]+)?$";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    /**
     * 是否是邮箱地址
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(email);
        return m.find();
    }


    /**
     * 检查是否重复按键
     *
     * @return
     */
    private static long curClickTime;

    public static boolean isRepeatClick() {
        boolean res = false;
        long click = System.currentTimeMillis();
        if (click - curClickTime < 1500) {
            res = true;
        }
        curClickTime = click;
        return res;
    }


    public static CharSequence setHighLightText(String content, int color, int start, int end) {

        SpannableString msp = new SpannableString(content);
        msp.setSpan(new ForegroundColorSpan(color), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return msp;
    }

    public static boolean isTopActiviy(Context context, String cmdName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(5);
        String cmpNameTemp = null;
        if (null != runningTaskInfos) {
            cmpNameTemp = (runningTaskInfos.get(0).topActivity).getClassName();
        }
        if (null == cmpNameTemp) return false;
        return cmpNameTemp.equals(cmdName);
    }


    public static String getTopActiviy(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(5);
        String cmpNameTemp = null;
        if (null != runningTaskInfos) {
            cmpNameTemp = (runningTaskInfos.get(0).topActivity).getClassName();
        }
        return cmpNameTemp;
    }

    public static String getTopPackage(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(5);
        String cmpNameTemp = null;
        if (null != runningTaskInfos) {
            cmpNameTemp = (runningTaskInfos.get(0).topActivity).getPackageName();
        }
        return cmpNameTemp;
    }

    public static boolean isActivityRunning(Context context, Class activityClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }


    public static boolean isActivityRunning(Context context, String actName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (actName.equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }


    /**
     * 获取总的接受字节数，包含Mobile和WiFi等
     *
     * @return KB
     */
    public static long getUidRxBytes(Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        long rxBytes = TrafficStats.getUidRxBytes(android.os.Process.myUid());
        return TrafficStats.getUidRxBytes(ai.uid) == TrafficStats.UNSUPPORTED ? 0 : rxBytes;
    }


    /**
     * 获取总的发送字节数，包含Mobile和WiFi等
     *
     * @return KB
     */
    public static long getUidTxBytes(Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        long txBytes = TrafficStats.getUidTxBytes(android.os.Process.myUid());
        return TrafficStats.getUidRxBytes(ai.uid) == TrafficStats.UNSUPPORTED ? 0 : txBytes;
    }

    /***
     * Toast LENGTH_LONG
     *
     * @param context
     * @param mToastText
     */
    public static void showLongToast(Context context, String mToastText) {
        Toast.makeText(context, mToastText, Toast.LENGTH_LONG).show();
    }

    /***
     * Toast LENGTH_LONG
     *
     * @param context
     * @param mToastText
     */
    public static void showShortToast(Context context, String mToastText) {
        Toast.makeText(context, mToastText, Toast.LENGTH_SHORT).show();
    }

    /***
     * 获取app应用的包名
     *
     * @param context
     */
    public static String getAppPackageName(Context context) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            // 当前版本的包名
            String packageNames = info.packageName;
            return packageNames;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 启动应用的设置,进入手动配置权限页面
     */
    public static void startAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }


    /**
     * 判断应该是否运行在前台
     *
     * @param context
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }


    public static Map<String, String> jsonToMap(JSONObject json) throws JSONException {
        Map<String, String> retMap = new HashMap<>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, String> toMap(JSONObject object) throws JSONException {
        Map<String, String> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            String value = (String) object.get(key);
            map.put(key, value);
        }
        return map;
    }


    public static boolean isJson(String str) {
        try {
            new JSONObject(str);
        } catch (JSONException ex) {
            try {
                new JSONArray(str);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
