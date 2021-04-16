package com.coocaa.tvpi.util;

import android.text.TextUtils;

import java.text.DecimalFormat;

/**
 * Created by IceStorm on 2017/12/18.
 */

public class TimeStringUtils {
    // a integer to xx:xx:xx
    public static String secToTime(long time) {
        String timeStr = null;
        long hour = 0;
        long minute = 0;
        long second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    /*public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }*/

    /*public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }*/
    public static String unitFormat(long i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Long.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }


    public static String convertIntToKW(int number){
        if(number >= 10000){
            return number/10000 + "万";
        }else if(number >= 1000){
            return number/1000 + "千";
        }else{
            return ""+number;
        }

    }

    public static String getTwoPointFloatStringByIntegerValue(int inputNum) {
        float resultValue = inputNum / 100f;
        DecimalFormat decimalFormat =new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String resultString = decimalFormat.format(resultValue);

        return resultString;
    }

    public static String getSeekString(int ms) {
        int h = ms / 1000 / (60 * 60);
        int m = ms / 1000 % (60 * 60) / (60);
        int s = ms / 1000 % 60;
        String hString = String.valueOf(h);
        String mString = String.valueOf(m);
        String sString = String.valueOf(s);
        if (h < 10) {
            if (h > 0)
                hString = "0" + h;
            else
                hString = "";
        }
        if (m < 10) {
            mString = "0" + m;
        }
        if (s < 10) {
            sString = "0" + s;
        }

        if (TextUtils.isEmpty(hString))
            return mString + ":" + sString;

        return hString + ":" + mString + ":" + sString;
    }

    public static String getTryWatchTime(int ms) {
        int m = ms / 1000 % (60 * 60) / (60);
        int s = ms / 1000 % 60;
        String mString = String.valueOf(m);
        String sString = String.valueOf(s);
        if (m < 10) {
            mString = "0" + m;
        }
        if (s < 10) {
            sString = "0" + s;
        }
        return "试看" + mString + "分" + sString + "秒";
    }
}
