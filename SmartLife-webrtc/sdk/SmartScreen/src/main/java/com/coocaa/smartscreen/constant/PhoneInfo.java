package com.coocaa.smartscreen.constant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * @Author: yuzhan
 */
public class PhoneInfo {

    /**
     * 品牌
     */
    public final String brand;
    /**
     * 型号
     */
    public final String model;
    /**
     * 制造商
     */
    public final String manufacturer;

    /**
     * 安卓版本
     */
    public final String androidVersion;

    private PhoneInfo(String brand, String model, String manufacturer, String androidVersion) {
        this.brand = brand;
        this.model = model;
        this.manufacturer = manufacturer;
        this.androidVersion = androidVersion;
        Log.d("Tvpi", "phone info : brand=" + brand + ", model=" + model + ", manufacturer=" + manufacturer + ", androidVersion=" + androidVersion);
    }

    public static class PhoneInfoBuilder {
        public static PhoneInfo.PhoneInfoBuilder builder() {
            return new PhoneInfo.PhoneInfoBuilder();
        }

        public PhoneInfo build() {
            return new PhoneInfo(getDeviceBrand(), getDeviceModel(), getDeviceManufacturer(), getDeviceAndroidVersion());
        }
    }


    /**
     * 获取厂商名
     * **/
    private static String getDeviceManufacturer() {
        return android.os.Build.MANUFACTURER;
    }

    /**
     * 获取产品名
     * **/
    private static String getDeviceProduct() {
        return android.os.Build.PRODUCT;
    }

    /**
     * 获取手机品牌
     */
    private static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机型号
     */
    private static String getDeviceModel() {
        return android.os.Build.MODEL;
    }


    /**
     * 获取设备的唯一标识， 需要 “android.permission.READ_Phone_STATE”权限
     */
    private static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        try {
            @SuppressLint("MissingPermission") String deviceId = tm.getDeviceId();
            if (deviceId == null) {
                return "Unknown";
            } else {
                return deviceId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }


    /**
     * ID
     *
     * **/
    private static String getDeviceId() {
        return android.os.Build.ID;
    }

    /**
     * 获取手机 硬件序列号
     * **/
    private static String getDeviceSerial() {
        return android.os.Build.SERIAL;
    }

    /**
     * 获取手机Android 系统SDK
     *
     * @return
     */
    private static int getDeviceSDK() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取手机Android 版本
     *
     * @return
     */
    private static String getDeviceAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }
}
