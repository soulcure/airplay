package com.coocaa.tvpi.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;

import com.coocaa.smartscreen.constant.SmartConstans;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String getRandomNumAndChacters(int length) {
        Random random = new Random();
        StringBuffer result = new StringBuffer();
        do {
            for (int i = 0; i < length; i++) {
                boolean b = random.nextBoolean();
                if (b) {
                    int choice = random.nextBoolean() ? 65 : 97; //随机到65：大写字母  97：小写字母
                    result.append((char) (choice + random.nextInt(26)));
                } else {
                    result.append(random.nextInt(10));
                }
            }
        } while (validate(result.toString()));//验证是否为字母和数字的组合
        return result.toString();
    }


    /**
     * 验证产生的随机字母数字组合是否是纯数字或者存字母
     *
     * @param str
     * @return true:纯字母或者纯数字组成；false：不是纯字母或者纯数字组成
     */
    public static boolean validate(String str) {
        Pattern pattern = Pattern.compile("^([0-9]+)|([A-Za-z]+)$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 返回当前程序版本号
     */
    public static String getAppVersionName(Context context) {
        String versionName = SmartConstans.getBuildInfo().versionName;
        try {
//            PackageManager pm = context.getPackageManager();
//            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
//            versionName = pi.versionName;
            if(!SmartConstans.getBuildInfo().publishMode) {//非挂包发布版本，增加编译日期显示
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                versionName = versionName + "-" + sdf.format(new Date(SmartConstans.getBuildInfo().buildTimestamp));
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName ;
    }

    public static int changeAlpha(int color, float fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int alpha = (int) (Color.alpha(color) * fraction);
        return Color.argb(alpha, red, green, blue);
    }
}
