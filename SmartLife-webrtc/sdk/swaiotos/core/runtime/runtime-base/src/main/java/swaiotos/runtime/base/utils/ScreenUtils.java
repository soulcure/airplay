package swaiotos.runtime.base.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * @Author: yuzhan
 */
public class ScreenUtils {
    private static boolean hasInit = false;
    private static boolean isLiuhaiScreen = false;

    //是否是刘海屏，
    public static boolean isLiuHaiScreen(Context context) {
        if(hasInit) {
            return isLiuhaiScreen;
        }
        isLiuhaiScreen = isLiuhaiOppo(context) || isLiuhaiHuawei(context) || isLiuhaiVivo(context) || isLiuhaiXiaomi(context) || isLiuhaiSmartisan(context);
        hasInit = true;
        return isLiuhaiScreen;
    }



    private static boolean isLiuhaiHuawei(Context context) {
        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            ret = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (Exception e) {
        }
        return ret;
    }

    private static boolean isLiuhaiVivo(Context context) {
        boolean ret = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class FtFeature = classLoader.loadClass("android.util.FtFeature");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            ret = (boolean) method.invoke(FtFeature, 0x00000020);
        } catch (Exception e) {
        }
        return ret;
    }

    private static boolean isLiuhaiOppo(Context context) {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    private static boolean isLiuhaiXiaomi(Context context) {
        return "1".equals(getProp("ro.miui.notch", ""));
    }

    private static boolean isLiuhaiSmartisan(Context context) {
        boolean ret = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class FtFeature = classLoader.loadClass("smartisanos.api.DisplayUtilsSmt");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            ret = (boolean) method.invoke(FtFeature, 0x00000001);
        } catch (Exception e) {
        }
        return ret;
    }

    private static void setProp(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("set", String.class, String.class );
            get.invoke(c, key, value );
        } catch (Exception e) {
        }
    }

    private static String getProp(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class );
            value = (String)(get.invoke(c, key, "unknown" ));
        } catch (Exception e) {
        }
        return value;
    }
}
