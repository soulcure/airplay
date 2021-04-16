package com.coocaa.swaiotos.virtualinput.utils;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

/**
 * @Description: 亮度调节工具类
 * @Author: wzh
 * @CreateDate: 2/2/21
 */
public class BrightnessTools {

    /**
     * 设置当前APP的亮度
     * screenBrightness 0 to 1 adjusts the brightness from dark to full bright
     */
    public static void setAppBrightness(Context context, float brightness) {
        Window window = ((Activity) context).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = brightness <= 0 ? 1 : brightness;
        }
        window.setAttributes(lp);
    }

    /**
     * 获取当前页面亮度
     *
     * @return
     */
    public static float getAppBrightness(Context context) {
        float brightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        if (context instanceof Activity) {
            Window window = ((Activity) context).getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            brightness = layoutParams.screenBrightness;
        }
        if (brightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            brightness = getSystemBrightness(context) / 255f;
        }
        if (brightness > 1) {
            brightness = brightness / 255f;
        }
        return brightness;
    }

    /**
     * 获取系统亮度
     */
    public static int getSystemBrightness(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
    }
}
