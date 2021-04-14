package com.skyworth.dpclientsdk;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class MACUtils {

    private static String mac;

    private static String getLocalEthernetMacAddress() {
        String mac = null;
        try {
            Enumeration localEnumeration = NetworkInterface.getNetworkInterfaces();

            while (localEnumeration.hasMoreElements()) {
                NetworkInterface localNetworkInterface = (NetworkInterface) localEnumeration.nextElement();
                String interfaceName = localNetworkInterface.getDisplayName();

                if (interfaceName == null) {
                    continue;
                }

                if (interfaceName.equals("eth0")) {
                    // MACAddr = convertMac(localNetworkInterface
                    // .getHardwareAddress());
                    mac = convertToMac(localNetworkInterface.getHardwareAddress());
                    if (mac != null && mac.startsWith("0:")) {
                        mac = "0" + mac;
                    }
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return mac;
    }

    private static String convertToMac(byte[] mac) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            byte b = mac[i];
            int value = 0;
            if (b >= 0 && b <= 16) {
                value = b;
                sb.append("0" + Integer.toHexString(value));
            } else if (b > 16) {
                value = b;
                sb.append(Integer.toHexString(value));
            } else {
                value = 256 + b;
                sb.append(Integer.toHexString(value));
            }
            if (i != mac.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    private static String getWifiMacAddr(Context context, String macAddr) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        if (null != info) {
            String addr = info.getMacAddress();
            if (null != addr) {
                Log.d("DEVINFO", "getWifiMacAddr:" + addr);
                macAddr = addr;
            }
        }
        return macAddr;
    }

    public static String getMac(Context context) {
        if (!TextUtils.isEmpty(mac) && !mac.equalsIgnoreCase("020000000000")) {
            return mac;
        }

        if (TextUtils.isEmpty(mac) || mac.equalsIgnoreCase("020000000000")) {
            mac = readFileByLines("/sys/class/net/eth0/address");
        }

        /*if (StringUtils.isEmpty(mac)) {
            mac = SystemProperties.get("third.get.mac","");
        }*/

        if (TextUtils.isEmpty(mac))
            mac = getMacFromHardware(context);

        if (TextUtils.isEmpty(mac)) {
            mac = getLocalEthernetMacAddress();//getWifiMacAddr(context,"");//getLocalEthernetMacAddress();
            Log.d("DEVINFO", "ethernet mac = " + mac);
        }

        if (TextUtils.isEmpty(mac)) {
            mac = getWifiMacAddr(context, mac);
            Log.d("DEVINFO", "wifi mac = " + mac);
        }

        if (!TextUtils.isEmpty(mac)) {
            mac = mac.replace(":", "");
            mac = mac.toLowerCase(Locale.getDefault());
        }

        if (mac != null) {
            mac = mac.toUpperCase();
        }
        return mac;
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

    /**
     * Android  6.0 之前（不包括6.0）
     * 必须的权限  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     *
     * @param context
     * @return
     */
    private static String getMacDefault(Context context) {
        String mac = null;
        if (context == null) {
            return mac;
        }

        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {

        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * Android 6.0（包括） - Android 7.0（不包括）
     *
     * @return
     */
    private static String getMacAddress() {
        String WifiAddress = null;
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }

    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     *
     * @return
     */
    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            Log.d("Utils", "all:" + all.size());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }
                Log.d("Utils", "macBytes:" + macBytes.length + "," + nif.getName());

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMacFromHardware(Context context) {

        String macAddress = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//5.0以下
            macAddress = getMacDefault(context);
            if (macAddress != null) {
                Log.d("Utils", "android 5.0以前的方式获取mac" + macAddress);
                macAddress = macAddress.replaceAll(":", "");
                if (macAddress.equalsIgnoreCase("020000000000") == false) {
                    return macAddress;
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            macAddress = getMacAddress();
            if (macAddress != null) {
                Log.d("Utils", "android 6~7 的方式获取的mac" + macAddress);
                macAddress = macAddress.replaceAll(":", "");
                if (macAddress.equalsIgnoreCase("020000000000") == false) {
                    return macAddress;
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            macAddress = getMacFromHardware();
            if (macAddress != null) {
                Log.d("Utils", "android 7以后 的方式获取的mac" + macAddress);
                macAddress = macAddress.replaceAll(":", "");
                if (macAddress.equalsIgnoreCase("020000000000") == false) {
                    return macAddress;
                }
            }
        }

        Log.d("Utils", "没有获取到MAC");
        return null;
    }

}
