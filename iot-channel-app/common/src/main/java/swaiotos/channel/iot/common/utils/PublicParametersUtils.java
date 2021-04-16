package swaiotos.channel.iot.common.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.server.utils.MACUtils;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.hardware.IScreen;
import swaiotos.sal.platform.IDeviceInfo;
import swaiotos.sal.platform.ISystemInfo;
import swaiotos.sal.system.ISystem;

/**
 * @author wagnyuehui
 * @time 2019/11/15
 * @describe
 */
public class PublicParametersUtils {

    public static final String QRCODE_BASE_URL = "https://ccss.tv/?";

    public static String getUserAgent(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return WebSettings.getDefaultUserAgent(ctx);
        }
        return new WebView(ctx).getSettings().getUserAgentString();
    }

    public static String getAndroidId(Context context) {

        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * 获取厂商
     *
     * @return 厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static int getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    public static String getMac(Context context) {
        if (!TextUtils.isEmpty(MAC))
            return MAC;
        try {
            String channel = Constants.getIOTChannel(context);
            if (channel.equals("TV")) {
                try {
                    IDeviceInfo deviceInfo = SAL.getModule(context, SalModule.DEVICE_INFO);
                    MAC = deviceInfo.getMac();
                } catch (Exception e) {
                    MAC = MACUtils.getMac(context);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    MAC = Settings.Global.getString(context.getContentResolver(), "swaiot_mac_key");
                }
                if (TextUtils.isEmpty(MAC)) {
                    MAC = MACUtils.getMac(context);
                    if (TextUtils.isEmpty(MAC)) {
                        MAC = "AAAAAAAAAAAA";
                    }
                }
            }
        } catch (Exception e) {
            MAC = "AAAAAAAAAAAA";
        }
        return MAC;
    }


    /***
     * 获取应用程序名称。
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /***
     * 获取应用程序包名
     * @param context
     * @return
     */
    public static String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 获取cUDID信息
     */
    private static String activeID = "";

    public static String getcUDID(Context context) {
        if (!TextUtils.isEmpty(activeID))
            return activeID;
        String channel = Constants.getIOTChannel(context);
        if (channel.equals("TV")) {
            ISystem iSystem = SAL.getModule(context, SalModule.SYSTEM);
            activeID = iSystem.getActiveId();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                activeID = Settings.Global.getString(context.getContentResolver(), "swaiot_activation_id_key");
            }
        }
        return activeID;
    }

    /**
     * 获取cModel信息
     */
    private static String cMode = "";

    public static String getcModel(Context context) {
        if (!TextUtils.isEmpty(cMode))
            return cMode;
        String channel = Constants.getIOTChannel(context);
        if (channel.equals("TV")) {
            //cMode = getDeviceModeMidType().skytype;
            IDeviceInfo deviceInfo = SAL.getModule(context, SalModule.DEVICE_INFO);
            cMode = deviceInfo.getModel();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                cMode = Settings.Global.getString(context.getContentResolver(), "swaiot_model_key");
            }
            if (TextUtils.isEmpty(cMode)) {
                cMode = Build.MODEL;
            }
        }
        return cMode;
    }

    /**
     * 获取芯片信息
     */
    private static String cChip = "";

    public static String getcChip(Context context) {
        if (!TextUtils.isEmpty(cChip))
            return cChip;
        String channel = Constants.getIOTChannel(context);
        if (channel.equals("TV")) {
            //cChip = getDeviceModeMidType().skymodel;
            IDeviceInfo deviceInfo = SAL.getModule(context, SalModule.DEVICE_INFO);
            cChip = deviceInfo.getChip();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                cChip = Settings.Global.getString(context.getContentResolver(), "swaiot_chip_key");
            }
            if (TextUtils.isEmpty(cChip)) {
                cChip = Build.BOARD;
            }
        }
        return cChip;
    }

    private static DeviceModeMidType mDeviceModeMidType = null;

    public static synchronized DeviceModeMidType getDeviceModeMidType() {
        if (mDeviceModeMidType == null) {
            String skymid = SystemProperties.get("ro.build.skymid");
            String skymodel = SystemProperties.get("ro.build.skymodel");
            String skytype = SystemProperties.get("ro.build.skytype");
            mDeviceModeMidType = new DeviceModeMidType(skymid, skymodel, skytype);
        }
        return mDeviceModeMidType;
    }

    public static synchronized String getDeviceSize() {
        String deviceSize = SystemProperties.get("third.get.barcode");
        if (deviceSize != null && !deviceSize.equals("")) {
            try {
                String size = deviceSize.trim().substring(0, 2);
                return size;
            } catch (Exception e) {
                return "0";
            }
        }
        return "0";
    }

    /**
     * 获取设备名称
     */
    private static String deviceName = "";

    public static String getdeviceName(Context context) {
        if (!TextUtils.isEmpty(deviceName))
            return deviceName;
        String channel = Constants.getIOTChannel(context);
        if (channel.equals("TV")) {
            ISystem iSystem = SAL.getModule(context, SalModule.SYSTEM);
            deviceName =  iSystem.getDeviceName();
        }
        if (TextUtils.isEmpty(deviceName)) {
            deviceName = Build.DEVICE;
        }
        if (!TextUtils.isEmpty(deviceName)) {
            try {
                deviceName = URLEncoder.encode(deviceName,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return deviceName;
    }

    /**
     * 获取cSize信息
     */
    public static String getcSize(Context context) {
        // return SystemProperties.get("persist.sys.panel_size");
        IScreen iScreen = SAL.getModule(context, SalModule.SCREEN);
        return iScreen.getPanelSize();
//        int width = context.getResources().getDisplayMetrics().widthPixels;
//        int height = context.getResources().getDisplayMetrics().heightPixels;
//
//        return width + "*" + height;
    }

    /**
     * 获取mac地址
     */
    private static String MAC = "";

    /**
     * 获取牌照商
     */
    public static String getcLicense() {

        return Build.DEVICE;
    }

    public static class DeviceModeMidType implements Serializable {
        public String skymid = "";
        public String skymodel = "";
        public String skytype = "";

        public DeviceModeMidType(String skymid, String skymodel, String skytype) {
            this.skymid = skymid;
            this.skymodel = skymodel;
            this.skytype = skytype;
        }

        public String toDeviceString() {
            return "[skymid:" + skymid + "][skymodel:" + skymodel + "][skytype:" + skytype + "]";
        }

    }



    public static String getURLAndBindCode(String bindCode) {
        return QRCODE_BASE_URL + "yw=kphd&m=sm" +"&bc="+ bindCode ;
    }

    private static final String HOMEPAGE_PKG_NAME_VER6 = "com.tianci.movieplatform";  // 6.0版本主页包名
    private static final String HOMEPAGE_PKG_NAME_PROP_KEY = "persist.service.homepage.pkg";

    public static int getHomepageVersion(Context context) {
        String pkgName = SystemProperties.get(HOMEPAGE_PKG_NAME_PROP_KEY, "");

        if (pkgName == null || pkgName.isEmpty() || !HOMEPAGE_PKG_NAME_VER6.equals(pkgName)) {
            System.out.println("getHomePageVersion: pkg name is empty or not equals ver 6");
            return -1;
        }

        return getAppVersionCode(context, pkgName);
    }

    private static int getAppVersionCode(Context context, String pkgName) {
        int versionCode = -1;
        PackageManager pm = context.getPackageManager();
        PackageInfo info;
        try {
            //防止因为pm为空导致的空指针异常
            if (pm != null) {
                info = pm.getPackageInfo(pkgName, 0);
                versionCode = info.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getcFMode() {
        String mode = SystemProperties.get("persist.sys.sf_mode");

        if (mode != null && mode.equals("Other")) {
            return "Other";
        }

        return "Default";
    }

    private static String coocaaVersion = null;

    public static String getcTcVersion(Context context) {
        ISystemInfo iSystemInfo = SAL.getModule(context, SalModule.SYSTEM_INFO);
        long versionCode = iSystemInfo.getVersionCode();

        return String.valueOf(versionCode);
        /*if (coocaaVersion == null || coocaaVersion.isEmpty()) {
            coocaaVersion = readFileByLines("/system/vendor/TianciVersion");
            if (coocaaVersion != null && !coocaaVersion.isEmpty()) {
                coocaaVersion = coocaaVersion.replace(".", "");
            }
        }
        return coocaaVersion;*/
    }


    public static String getcPattern() {
        String pattern = SystemProperties.get("third.get.system.scene");
        if (pattern == null || pattern.isEmpty()) {
            pattern = "normal";
        } else if (pattern.contains("child")) {
            pattern = "child";
        } else if (pattern.contains("aged")) {
            pattern = "aged";
        }

        return pattern;
    }

    public static String getResolution(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        if (display == null) {
            return null;
        }

        int width = display.getWidth();
        int height = display.getHeight();

        return width + "x" + height;  // result: 1920x1080 or 1280x720
    }

    public static String getSDK() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    public static String getcEmmcCID(Context context) {

        /*String emmcID = SystemProperties.get("third.get.cid");

        if (emmcID == null || emmcID.isEmpty()) {
            emmcID = readFileByLines("/sys/block/mmcblk0/device/cid");
        }

        return emmcID;*/
        IDeviceInfo deviceInfo = SAL.getModule(context, SalModule.DEVICE_INFO);
        return deviceInfo.getDeviceID();
    }

    public static String getcBrand(Context context) {
        //return SystemProperties.get("ro.product.brand");
        IDeviceInfo deviceInfo = SAL.getModule(context, SalModule.DEVICE_INFO);
        return deviceInfo.getBrand();
    }

    private static String readFileByLines(String fileName) {
        String content = "";

        File file = new File(fileName);
        if (!file.exists()) {
            return content;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                content += tempString;
                System.out.println("line " + line + ": " + tempString);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return content;
    }

}
