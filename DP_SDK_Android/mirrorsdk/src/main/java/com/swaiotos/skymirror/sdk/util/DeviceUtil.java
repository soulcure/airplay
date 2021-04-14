package com.swaiotos.skymirror.sdk.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @ClassName: DeviceUtil
 * @Author: AwenZeng
 * @CreateDate: 2020/3/19 10:38
 * @Description: 设备信息工具类
 */
public class DeviceUtil {
//    /**
//     * 获取设备唯一识别码
//     */
//    public static String getDeviceId(Context context) {
//        String deviceId = "";
//
//        deviceId = SystemProperty.getDeviceId();
//
//        if (EmptyUtils.isEmpty(deviceId)) {
//            deviceId = getImei(context);
//        }
//
//        if (EmptyUtils.isEmpty(deviceId)) {
//            deviceId = MacUtils.getMAC(context);
//        }
//        return deviceId;
//    }
//
//    /**
//     * imei
//     *
//     * @return
//     */
//    public static String getImei(Context context) {
//        try {
//            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            return tm.getDeviceId();
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//        return "SSE-DEMO-" + Math.random() * 10000000;
//    }

    //    public static String getLocalIPAddress() {
//        try {
//            Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
//            InetAddress ip = null;
//            while (allNetInterfaces.hasMoreElements()) {
//                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
//                Log.d("NET", netInterface.getName());
//                Enumeration addresses = netInterface.getInetAddresses();
//                while (addresses.hasMoreElements()) {
//                    ip = (InetAddress) addresses.nextElement();
//                    if (ip != null && ip instanceof Inet4Address && !ip.getHostAddress().toString().contains("127.0.0.1")) {
//                        System.out.println("本机的IP = " + ip.getHostAddress());
//                        return ip.getHostAddress();
//                    }
//                }
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
    public static String getLocalIPAddress(Context context) {
        String  ip = getWifiIPAddress(context);
        if (TextUtils.isEmpty(ip) || TextUtils.equals("0.0.0.0", ip)) {
            ip = getEthIPAddress();
        }
        return ip;
    }

    public static String getWifiIPAddress(Context context) {
//获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    public static String getEthIPAddress() {
        try {
            Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                String interfaceName = netInterface.getDisplayName();
                Log.d("NET", interfaceName);
                if (interfaceName.equals("eth0")) {
                    Enumeration addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = (InetAddress) addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address && !ip.getHostAddress().toString().contains("127.0.0.1")) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
}
