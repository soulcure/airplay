package com.coocaa.statemanager.view.windowview;

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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @ Created on: 2020/10/24
 * @Author: LEGION XiaoLuo
 * @ Description:
 */
public class StateMacUtils {

    private static String mac;

    public StateMacUtils() {
    }

    private static String getLocalEthernetMacAddress() {
        String mac = null;

        try {
            Enumeration localEnumeration = NetworkInterface.getNetworkInterfaces();

            while(localEnumeration.hasMoreElements()) {
                NetworkInterface localNetworkInterface = (NetworkInterface)localEnumeration.nextElement();
                String interfaceName = localNetworkInterface.getDisplayName();
                if (interfaceName != null && interfaceName.equals("eth0")) {
                    mac = convertToMac(localNetworkInterface.getHardwareAddress());
                    if (mac != null && mac.startsWith("0:")) {
                        mac = "0" + mac;
                    }
                    break;
                }
            }
        } catch (SocketException var4) {
            var4.printStackTrace();
        }

        return mac;
    }

    private static String convertToMac(byte[] mac) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < mac.length; ++i) {
            byte b = mac[i];
            if (b >= 0 && b <= 16) {
                sb.append("0" + Integer.toHexString(b));
            } else if (b > 16) {
                sb.append(Integer.toHexString(b));
            } else {
                int value = 256 + b;
                sb.append(Integer.toHexString(value));
            }

            if (i != mac.length - 1) {
                sb.append(":");
            }
        }

        return sb.toString();
    }

    private static String getWifiMacAddr(Context context, String macAddr) {
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
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
        } else {
            if (TextUtils.isEmpty(mac) || mac.equalsIgnoreCase("020000000000")) {
                mac = readFileByLines("/sys/class/net/eth0/address");
            }

            if (TextUtils.isEmpty(mac)) {
                mac = getMacFromHardware(context);
            }

            if (TextUtils.isEmpty(mac)) {
                mac = getLocalEthernetMacAddress();
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
    }

    private static String readFileByLines(String fileName) {
        String content = "";
        File file = new File(fileName);
        if (!file.exists()) {
            return content;
        } else {
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new FileReader(file));

                String tempString;
                for(int line = 1; (tempString = reader.readLine()) != null; ++line) {
                    content = content + tempString;
                    System.out.println("line " + line + ": " + tempString);
                }

                reader.close();
            } catch (IOException var14) {
                var14.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException var13) {
                        var13.printStackTrace();
                    }
                }

            }

            return content;
        }
    }

    private static String getMacDefault(Context context) {
        String mac = null;
        if (context == null) {
            return mac;
        } else {
            WifiManager wifi = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifi == null) {
                return mac;
            } else {
                WifiInfo info = null;

                try {
                    info = wifi.getConnectionInfo();
                } catch (Exception var5) {
                }

                if (info == null) {
                    return null;
                } else {
                    mac = info.getMacAddress();
                    if (!TextUtils.isEmpty(mac)) {
                        mac = mac.toUpperCase(Locale.ENGLISH);
                    }

                    return mac;
                }
            }
        }
    }

    private static String getMacAddress() {
        String WifiAddress = null;

        try {
            WifiAddress = (new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address")))).readLine();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

        return WifiAddress;
    }

    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            Log.d("Utils", "all:" + all.size());
            Iterator var1 = all.iterator();

            while(var1.hasNext()) {
                NetworkInterface nif = (NetworkInterface)var1.next();
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return null;
                    }

                    Log.d("Utils", "macBytes:" + macBytes.length + "," + nif.getName());
                    StringBuilder res1 = new StringBuilder();
                    byte[] var5 = macBytes;
                    int var6 = macBytes.length;

                    for(int var7 = 0; var7 < var6; ++var7) {
                        byte b = var5[var7];
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }

                    return res1.toString();
                }
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return null;
    }

    public static String getMacFromHardware(Context context) {
        String macAddress = null;
        if (Build.VERSION.SDK_INT < 23) {
            macAddress = getMacDefault(context);
            if (macAddress != null) {
                Log.d("Utils", "android 5.0以前的方式获取mac" + macAddress);
                macAddress = macAddress.replaceAll(":", "");
                if (!macAddress.equalsIgnoreCase("020000000000")) {
                    return macAddress;
                }
            }
        } else if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 24) {
            macAddress = getMacAddress();
            if (macAddress != null) {
                Log.d("Utils", "android 6~7 的方式获取的mac" + macAddress);
                macAddress = macAddress.replaceAll(":", "");
                if (!macAddress.equalsIgnoreCase("020000000000")) {
                    return macAddress;
                }
            }
        } else if (Build.VERSION.SDK_INT >= 24) {
            macAddress = getMacFromHardware();
            if (macAddress != null) {
                Log.d("Utils", "android 7以后 的方式获取的mac" + macAddress);
                macAddress = macAddress.replaceAll(":", "");
                if (!macAddress.equalsIgnoreCase("020000000000")) {
                    return macAddress;
                }
            }
        }

        Log.d("Utils", "没有获取到MAC");
        return null;
    }
}
