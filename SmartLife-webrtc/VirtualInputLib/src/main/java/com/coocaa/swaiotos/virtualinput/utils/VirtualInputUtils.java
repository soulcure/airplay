package com.coocaa.swaiotos.virtualinput.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;

import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.IUserInfo;

import swaiotos.sensor.data.AccountInfo;

/**
 * @Author: yuzhan
 */
public class VirtualInputUtils {

    protected static Vibrator vibrator;
    protected static long VIBRATE_DURATION = 100L;
    protected static long ONETSHOT_VIBRATE_DURATION = 40;

    public static void init(Context context) {
        if(vibrator == null) {
            vibrator = (Vibrator) context.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public static void playVibrate() {
        if(vibrator != null) {
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    public static void playVibrateOneShot() {
        if(vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ONETSHOT_VIBRATE_DURATION, 10));
            } else {
                vibrator.vibrate(ONETSHOT_VIBRATE_DURATION);
            }
        }
    }

    private final static String GUIDE_FLAG = "vi_guide_flag_new";

    public static boolean needShowGuide(Context context) {
        return !TextUtils.equals("true", SuperSpUtil.getString(context, GUIDE_FLAG));
    }

    public static void onGuideShowFinish(Context context) {
        SuperSpUtil.putString(context, GUIDE_FLAG, "true");
    }

    private final static String DOT_GUIDE_FLAG = "vi_dot_guide_flag_new";
    public static boolean needShowDotGuide(Context context) {
        return !TextUtils.equals("true", SuperSpUtil.getString(context, DOT_GUIDE_FLAG));
    }

    public static void onDotGuideShowFinish(Context context) {
        SuperSpUtil.putString(context, DOT_GUIDE_FLAG, "true");
    }

    public static AccountInfo getAccountInfo() {
        AccountInfo info = new AccountInfo();
        IUserInfo userInfo = SmartApi.getUserInfo();
        if (userInfo != null) {
            info.accessToken = userInfo.accessToken;
            info.avatar = userInfo.avatar;
            info.mobile = userInfo.mobile;
            info.open_id = userInfo.open_id;
            info.nickName = userInfo.nickName;
        }
        return info;
    }
}
